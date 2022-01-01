package cn.icodening.logstash.plugin.input.registry;

import java.util.Map;

public interface ServiceInstance {

    default String getInstanceId() {
        return null;
    }

    String getServiceId();

    String getHost();

    int getPort();

    default String getHostPort() {
        return getHost() + ":" + getPort();
    }

    Map<String, String> getMetadata();
}
