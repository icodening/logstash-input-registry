package cn.icodening.logstash.plugin.input.registry;

import java.util.List;

public interface RegistryService {

    String name();

    List<String> getServices();

    List<ServiceInstance> getInstances(String serviceId);

    void subscribe(String service, RegistryEventListener eventListener);

    void unsubscribe(String serviceName, RegistryEventListener listener);

    List<String> getSubscribed();
}
