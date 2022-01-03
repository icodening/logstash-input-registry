package cn.icodening.logstash.plugin.input.registry.eureka;

import cn.icodening.logstash.plugin.input.registry.*;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.CacheRefreshedEvent;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class EurekaRegistryService implements RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaRegistryService.class);

    private final Map<String, List<RegistryEventListener>> listenerMap = new ConcurrentHashMap<>();

    private final Map<String, List<ServiceInstance>> oldInstancesMap = new ConcurrentHashMap<>();

    private volatile String lastHashCode;

    private final EurekaClient eurekaClient;

    public EurekaRegistryService(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
        eurekaClient.registerEventListener(event -> {
            if (event instanceof CacheRefreshedEvent) {
                dispatchRefreshedEvent();
            }
        });
    }

    private void dispatchRefreshedEvent() {
        LOGGER.info("receive refresh event");
        Applications applications = eurekaClient.getApplications();
        String appsHashCode = applications.getAppsHashCode();
        if (!Objects.equals(lastHashCode, appsHashCode)) {
            lastHashCode = appsHashCode;
            LOGGER.info("eureka start refresh");
            Set<Map.Entry<String, List<RegistryEventListener>>> appListeners = listenerMap.entrySet();
            for (Map.Entry<String, List<RegistryEventListener>> appListener : appListeners) {
                String appName = appListener.getKey();
                List<RegistryEventListener> registryEventListeners = appListener.getValue();
                if (registryEventListeners == null || registryEventListeners.isEmpty()) {
                    continue;
                }

                Application newApplication = applications.getRegisteredApplications(appName);
                List<ServiceInstance> oldInstances = oldInstancesMap.get(appName);
                if (oldInstances == null) {
                    oldInstances = Collections.emptyList();
                }

                List<ServiceInstance> newInstances = Collections.emptyList();
                if (newApplication != null) {
                    newInstances = newApplication.getInstances()
                            .stream()
                            .map(EurekaRegistryService::mapping)
                            .collect(Collectors.toList());
                }
                CompareResult compareResult = ServicesComparator.compareServices(oldInstances, newInstances);
                List<ServiceInstance> addedList = compareResult.getAddedList();
                List<ServiceInstance> changedList = compareResult.getChangedList();
                List<ServiceInstance> removedList = compareResult.getRemovedList();
                LOGGER.info(appName + " added: " + addedList);
                LOGGER.info(appName + " changed: " + changedList);
                LOGGER.info(appName + " removed: " + removedList);
                if (!addedList.isEmpty()) {
                    for (RegistryEventListener registryEventListener : registryEventListeners) {
                        registryEventListener.onEvent(new RegistryEvent(addedList, EventType.ADDED));
                    }
                }
                if (!changedList.isEmpty()) {
                    for (RegistryEventListener registryEventListener : registryEventListeners) {
                        registryEventListener.onEvent(new RegistryEvent(changedList, EventType.CHANGED));
                    }
                }
                if (!removedList.isEmpty()) {
                    for (RegistryEventListener registryEventListener : registryEventListeners) {
                        registryEventListener.onEvent(new RegistryEvent(removedList, EventType.REMOVED));
                    }
                }
                oldInstancesMap.put(appName, newInstances);
            }
            LOGGER.info("eureka refresh finished");
        }
    }

    @Override
    public String name() {
        return "eureka";
    }

    @Override
    public List<String> getServices() {
        Applications applications = eurekaClient.getApplications();
        List<Application> registeredApplications = applications.getRegisteredApplications();
        return registeredApplications.stream()
                .map(Application::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        Application application = eurekaClient.getApplication(serviceId);
        return application.getInstancesAsIsFromEureka()
                .stream()
                .map(EurekaRegistryService::mapping)
                .collect(Collectors.toList());
    }

    @Override
    public void subscribe(String service, RegistryEventListener eventListener) {
        List<RegistryEventListener> registryEventListeners = listenerMap.get(service);
        if (registryEventListeners == null) {
            listenerMap.putIfAbsent(service, new CopyOnWriteArrayList<>());
            registryEventListeners = listenerMap.get(service);
        }
        registryEventListeners.add(eventListener);
    }

    @Override
    public void unsubscribe(String serviceName, RegistryEventListener listener) {
        List<RegistryEventListener> registryEventListeners = listenerMap.get(serviceName);
        if (registryEventListeners == null
                || registryEventListeners.isEmpty()) {
            return;
        }
        registryEventListeners.remove(listener);
    }

    @Override
    public List<String> getSubscribed() {
        return new ArrayList<>(listenerMap.keySet());
    }

    private static ServiceInstance mapping(InstanceInfo instance) {
        if (instance == null) {
            return null;
        }
        DefaultServiceInstance.Builder builder = DefaultServiceInstance.builder();
        return builder.host(instance.getIPAddr())
                .port(instance.getPort())
                .serviceId(instance.getAppName())
                .metadata(instance.getMetadata())
                .build();
    }
}
