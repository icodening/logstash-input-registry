package cn.icodening.logstash.plugin.input.registry;

import java.util.List;

public interface Registry {

    List<String> getServices();

    List<ServiceInstance> getInstances(String serviceId);

    void subscribe(String service, NamingEventListener eventListener);

    void unsubscribe(String serviceName, NamingEventListener listener);

    List<String> getSubscribed();
}
