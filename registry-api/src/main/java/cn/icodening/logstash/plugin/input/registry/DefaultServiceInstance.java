package cn.icodening.logstash.plugin.input.registry;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class DefaultServiceInstance implements ServiceInstance {

    private String serviceId;

    private String host;

    private int port;

    private Map<String, String> metadata = Collections.emptyMap();

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultServiceInstance that = (DefaultServiceInstance) o;
        return port == that.port &&
                serviceId.equals(that.serviceId) &&
                host.equals(that.host) &&
                metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, host, port, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final DefaultServiceInstance serviceInstance = new DefaultServiceInstance();

        private Builder() {

        }

        public ServiceInstance build() {
            return serviceInstance;
        }

        public Builder serviceId(String serviceId) {
            serviceInstance.serviceId = serviceId;
            return this;
        }

        public Builder host(String host) {
            serviceInstance.host = host;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            serviceInstance.metadata = metadata;
            return this;
        }

        public Builder port(int port) {
            serviceInstance.port = port;
            return this;
        }
    }
}
