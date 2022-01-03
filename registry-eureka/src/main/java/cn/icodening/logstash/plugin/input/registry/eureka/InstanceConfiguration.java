package cn.icodening.logstash.plugin.input.registry.eureka;

import com.netflix.appinfo.AbstractInstanceConfig;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author icodening
 * @date 2022.01.03
 */
public class InstanceConfiguration extends AbstractInstanceConfig implements EurekaInstanceConfig {

    private final DynamicPropertyFactory configInstance;
    private final String namespace;
    private final String appGrpNameFromEnv;

    public InstanceConfiguration(AbstractConfiguration configuration) {
        this("eureka.", configuration);
    }

    public InstanceConfiguration(DynamicPropertyFactory configInstance) {
        this("eureka.", configInstance);
    }

    public InstanceConfiguration(String namespace, AbstractConfiguration configuration) {
        this.namespace = namespace;
        this.configInstance = DynamicPropertyFactory.initWithConfigurationSource(configuration);
        this.appGrpNameFromEnv = ConfigurationManager.getConfigInstance()
                .getString("NETFLIX_APP_GROUP", "unknown");
    }

    public InstanceConfiguration(String namespace, DynamicPropertyFactory configInstance) {
        this.namespace = namespace;
        this.configInstance = configInstance;
        this.appGrpNameFromEnv = ConfigurationManager.getConfigInstance()
                .getString("NETFLIX_APP_GROUP", "unknown");
    }

    @Override
    public boolean isInstanceEnabledOnit() {
        return configInstance.getBooleanProperty(namespace + "traffic.enabled",
                super.isInstanceEnabledOnit()).get();
    }


    @Override
    public int getNonSecurePort() {
        return configInstance.getIntProperty(namespace + "port", super.getNonSecurePort()).get();
    }


    @Override
    public int getSecurePort() {
        return configInstance.getIntProperty(namespace + "securePort", super.getSecurePort()).get();
    }


    @Override
    public boolean isNonSecurePortEnabled() {
        return configInstance.getBooleanProperty(namespace + "port.enabled", super.isNonSecurePortEnabled()).get();
    }


    @Override
    public boolean getSecurePortEnabled() {
        return configInstance.getBooleanProperty(namespace + "securePort.enabled",
                super.getSecurePortEnabled()).get();
    }


    @Override
    public int getLeaseRenewalIntervalInSeconds() {
        return configInstance.getIntProperty(namespace + "lease.renewalInterval",
                super.getLeaseRenewalIntervalInSeconds()).get();
    }


    @Override
    public int getLeaseExpirationDurationInSeconds() {
        return configInstance.getIntProperty(namespace + "lease.duration",
                super.getLeaseExpirationDurationInSeconds()).get();
    }


    @Override
    public String getVirtualHostName() {
        if (this.isNonSecurePortEnabled()) {
            return configInstance.getStringProperty(namespace + "vipAddress",
                    super.getVirtualHostName()).get();
        } else {
            return null;
        }
    }

    @Override
    public String getSecureVirtualHostName() {
        if (this.getSecurePortEnabled()) {
            return configInstance.getStringProperty(namespace + "secureVipAddress",
                    super.getSecureVirtualHostName()).get();
        } else {
            return null;
        }
    }

    @Override
    public String getASGName() {
        return configInstance.getStringProperty(namespace + "asgName", super.getASGName()).get();
    }

    @Override
    public Map<String, String> getMetadataMap() {
        String metadataNamespace = namespace + "metadata" + ".";
        Map<String, String> metadataMap = new LinkedHashMap<>();
        Configuration config = (Configuration) configInstance.getBackingConfigurationSource();
        String subsetPrefix = metadataNamespace.charAt(metadataNamespace.length() - 1) == '.'
                ? metadataNamespace.substring(0, metadataNamespace.length() - 1)
                : metadataNamespace;
        for (Iterator<String> iter = config.subset(subsetPrefix).getKeys(); iter.hasNext(); ) {
            String key = iter.next();
            String value = config.getString(subsetPrefix + "." + key);
            metadataMap.put(key, value);
        }
        return metadataMap;
    }

    @Override
    public String getInstanceId() {
        String result = configInstance.getStringProperty(namespace + "instanceId", null).get();
        return result == null ? null : result.trim();
    }

    @Override
    public String getAppname() {
        return configInstance.getStringProperty(namespace + "name", "unknown").get().trim();
    }

    @Override
    public String getAppGroupName() {
        return configInstance.getStringProperty(namespace + "appGroup", appGrpNameFromEnv).get().trim();
    }

    public String getIpAddress() {
        return super.getIpAddress();
    }


    @Override
    public String getStatusPageUrlPath() {
        return configInstance.getStringProperty(namespace + "statusPageUrlPath",
                "/Status").get();
    }

    @Override
    public String getStatusPageUrl() {
        return configInstance.getStringProperty(namespace + "statusPageUrl", null)
                .get();
    }


    @Override
    public String getHomePageUrlPath() {
        return configInstance.getStringProperty(namespace + "homePageUrlPath",
                "/").get();
    }

    @Override
    public String getHomePageUrl() {
        return configInstance.getStringProperty(namespace + "homePageUrl", null)
                .get();
    }

    @Override
    public String getHealthCheckUrlPath() {
        return configInstance.getStringProperty(namespace + "healthCheckUrlPath",
                "/healthcheck").get();
    }

    @Override
    public String getHealthCheckUrl() {
        return configInstance.getStringProperty(namespace + "healthCheckUrl", null)
                .get();
    }

    @Override
    public String getSecureHealthCheckUrl() {
        return configInstance.getStringProperty(namespace + "secureHealthCheckUrl",
                null).get();
    }

    @Override
    public String[] getDefaultAddressResolutionOrder() {
        String result = configInstance.getStringProperty(namespace + "defaultAddressResolutionOrder", null).get();
        return result == null ? new String[0] : result.split(",");
    }

    /**
     * Indicates if the public ipv4 address of the instance should be advertised.
     *
     * @return true if the public ipv4 address of the instance should be advertised, false otherwise .
     */
    public boolean shouldBroadcastPublicIpv4Addr() {
        return configInstance.getBooleanProperty(namespace + "broadcastPublicIpv4", super.shouldBroadcastPublicIpv4Addr()).get();
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }
}
