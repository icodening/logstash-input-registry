package cn.icodening.logstash.plugin.input.registry;

import java.net.URI;
import java.util.Map;

public interface ServiceInstance {

    default String getInstanceId() {
        return null;
    }

    String getServiceId();

    String getHost();

    int getPort();

    Map<String, String> getMetadata();
}
