package cn.icodening.logstash.plugin.input.registry.nacos;

import cn.icodening.logstash.plugin.input.registry.*;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NacosRegistryService implements RegistryService {

    private final Map<RegistryEventListener, EventListener> listenerMap = new ConcurrentHashMap<>();

    private final Map<String, List<ServiceInstance>> servicesInstances = new ConcurrentHashMap<>();

    private final NamingService namingService;

    public NacosRegistryService(NamingService namingService) {
        this.namingService = namingService;
    }

    @Override
    public String name() {
        return "nacos";
    }

    @Override
    public List<String> getServices() {
        try {
            return namingService.getServicesOfServer(1, Integer.MAX_VALUE).getData();
        } catch (Throwable e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        try {
            List<Instance> allInstances = namingService.getAllInstances(serviceId);
            if (allInstances == null || allInstances.isEmpty()) {
                return Collections.emptyList();
            }
            return allInstances.stream().map(NacosRegistryService::mapping).collect(Collectors.toList());
        } catch (Throwable e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void subscribe(String service, RegistryEventListener eventListener) {
        try {
            EventListener ens = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent ne = (NamingEvent) event;
                    List<Instance> instances = ne.getInstances();
                    if (instances == null || instances.isEmpty()) {
                        return;
                    }
                    List<ServiceInstance> newInstances = instances.stream().map(NacosRegistryService::mapping).collect(Collectors.toList());
                    List<ServiceInstance> oldInstances = servicesInstances.get(service);
                    if (oldInstances == null) {
                        oldInstances = Collections.emptyList();
                    }

                    CompareResult result = ServicesComparator.compareServices(oldInstances, newInstances);
                    List<ServiceInstance> addedInstances = result.getAddedList();
                    List<ServiceInstance> updateInstances = result.getChangedList();
                    List<ServiceInstance> removedInstances = result.getRemovedList();

                    if (!addedInstances.isEmpty()) {
                        eventListener.onEvent(new RegistryEvent(addedInstances, EventType.ADDED));
                    }
                    if (!updateInstances.isEmpty()) {
                        eventListener.onEvent(new RegistryEvent(updateInstances, EventType.CHANGED));
                    }
                    if (!removedInstances.isEmpty()) {
                        eventListener.onEvent(new RegistryEvent(removedInstances, EventType.REMOVED));
                    }
                    servicesInstances.put(service, newInstances);
                }
            };
            listenerMap.putIfAbsent(eventListener, ens);
            namingService.subscribe(service, ens);
        } catch (Throwable e) {
            //TODO LOG
        }
    }

    @Override
    public void unsubscribe(String serviceName, RegistryEventListener listener) {
        EventListener eventListener = listenerMap.get(listener);
        if (eventListener == null) {
            return;
        }
        EventListener remove = listenerMap.remove(listener);
        try {
            namingService.unsubscribe(serviceName, remove);
        } catch (Throwable e) {
            //TODO LOG
        }
    }

    @Override
    public List<String> getSubscribed() {
        try {
            List<ServiceInfo> subscribeServices = namingService.getSubscribeServices();
            if (subscribeServices == null || subscribeServices.isEmpty()) {
                return Collections.emptyList();
            }
            return subscribeServices.stream().map(ServiceInfo::getName).collect(Collectors.toList());
        } catch (NacosException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static ServiceInstance mapping(Instance instance) {
        if (instance == null) {
            return null;
        }
        DefaultServiceInstance.Builder builder = DefaultServiceInstance.builder();
        return builder.host(instance.getIp())
                .port(instance.getPort())
                .serviceId(instance.getServiceName())
                .metadata(instance.getMetadata())
                .build();
    }

}
