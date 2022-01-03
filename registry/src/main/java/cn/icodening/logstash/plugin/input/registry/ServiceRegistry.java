package cn.icodening.logstash.plugin.input.registry;

import co.elastic.logstash.api.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@LogstashPlugin(name = "service_registry")
public class ServiceRegistry implements Input {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Logger logger;

    private final String id;

    private final Configuration config;

    private final Context context;

    private final Collection<PluginConfigSpec<?>> specs;

    private final CountDownLatch cdl = new CountDownLatch(1);

    // all plugins must provide a constructor that accepts String id, Configuration, and Context
    public ServiceRegistry(String id, Configuration config, Context context) {
        this.id = id;
        this.config = config;
        this.context = context;
        this.logger = context.getLogger(this);
        ServiceLoader<PluginConfigSpecDefiner> definers = ServiceLoader.load(PluginConfigSpecDefiner.class);
        List<PluginConfigSpec<?>> configSchemas = new ArrayList<>();
        for (PluginConfigSpecDefiner definer : definers) {
            configSchemas.add(definer.getPluginConfigSpecSchema());
        }
        specs = configSchemas;
    }

    @Override
    public void start(Consumer<Map<String, Object>> consumer) {
        ServiceLoader<RegistryServiceFactory> load = ServiceLoader.load(RegistryServiceFactory.class);
        List<RegistryService> registryServices = new ArrayList<>();
        for (RegistryServiceFactory registryServiceFactory : load) {
            RegistryService registryService = registryServiceFactory.createRegistryService(config);
            if (registryService == null) {
                continue;
            }
            registryServices.add(registryService);
        }
        if (registryServices.isEmpty()) {
            return;
        }
        final ScheduledExecutorService fetchAllServicesExecutor = Executors.newScheduledThreadPool(registryServices.size(), r -> {
            Thread thread = new Thread(r);
            thread.setName("fetch-services-thread");
            return thread;
        });
        for (RegistryService registryService : registryServices) {
            fetchAllServicesExecutor.scheduleAtFixedRate(() -> {
                try {
                    List<String> services = registryService.getServices();
                    List<String> subscribed = registryService.getSubscribed();
                    logger.info("get services from " + registryService.name() + ": " + services);
                    Set<String> subscribedSet = new HashSet<>(subscribed);
                    for (String service : services) {
                        if (subscribedSet.add(service)) {
                            logger.info("subscribe service [" + service + "]");
                            registryService.subscribe(service, event -> {
                                EventType eventType = event.eventType();
                                Map<String, Object> data = new HashMap<>();
                                List<ServiceInstance> serviceInstances = event.getSource();
                                data.put("source", registryService.name());
                                data.put("service", service);
                                data.put("type", eventType.toString());
                                try {
                                    data.put("instances", OBJECT_MAPPER.writeValueAsString(serviceInstances));
                                    consumer.accept(data);
                                } catch (JsonProcessingException e) {
                                    logger.error("json pass instances error !!!", e);
                                }
                            });
                            //订阅完成之后先发送一次add事件
                            Map<String, Object> data = new HashMap<>();
                            List<ServiceInstance> serviceInstances = registryService.getInstances(service);
                            data.put("source", registryService.name());
                            data.put("service", service);
                            data.put("type", EventType.ADDED.toString());
                            try {
                                data.put("instances", OBJECT_MAPPER.writeValueAsString(serviceInstances));
                                consumer.accept(data);
                            } catch (JsonProcessingException e) {
                                logger.error("json pass instances error !!!", e);
                            }
                        }
                    }
                } catch (Throwable e) {
                    logger.error("fetch services task error !!!", e);
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
        try {
            cdl.await();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void stop() {
        cdl.countDown();
    }

    @Override
    public void awaitStop() throws InterruptedException {
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return specs;
    }

    @Override
    public String getId() {
        return id;
    }
}
