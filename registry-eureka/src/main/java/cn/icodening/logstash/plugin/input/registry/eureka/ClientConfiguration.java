package cn.icodening.logstash.plugin.input.registry.eureka;

import com.netflix.appinfo.EurekaAccept;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.shared.transport.DefaultEurekaTransportConfig;
import com.netflix.discovery.shared.transport.EurekaTransportConfig;
import org.apache.commons.configuration.AbstractConfiguration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author icodening
 * @date 2022.01.03
 */
public class ClientConfiguration implements EurekaClientConfig {

    public static final String DEFAULT_ZONE = "defaultZone";
    public static final String URL_SEPARATOR = "\\s*,\\s*";

    private final DynamicPropertyFactory configInstance;
    private final String namespace;
    private final EurekaTransportConfig transportConfig;

    public ClientConfiguration(String namespace, AbstractConfiguration configuration) {
        this(namespace, DynamicPropertyFactory.initWithConfigurationSource(configuration));
    }

    public ClientConfiguration(AbstractConfiguration configuration) {
        this(DynamicPropertyFactory.initWithConfigurationSource(configuration));
    }

    public ClientConfiguration(DynamicPropertyFactory configInstance) {
        this("eureka.", configInstance);
    }

    public ClientConfiguration(String namespace, DynamicPropertyFactory configInstance) {
        this.namespace = namespace;
        this.configInstance = configInstance;
        this.transportConfig = new DefaultEurekaTransportConfig(namespace, configInstance);

    }


    @Override
    public int getRegistryFetchIntervalSeconds() {
        return configInstance.getIntProperty(
                namespace + "client.refresh.interval", 30).get();
    }


    @Override
    public int getInstanceInfoReplicationIntervalSeconds() {
        return configInstance.getIntProperty(
                namespace + "appinfo.replicate.interval", 30).get();
    }

    @Override
    public int getInitialInstanceInfoReplicationIntervalSeconds() {
        return configInstance.getIntProperty(
                namespace + "appinfo.initial.replicate.time", 40).get();
    }

    @Override
    public int getEurekaServiceUrlPollIntervalSeconds() {
        return configInstance.getIntProperty(
                namespace + "serviceUrlPollIntervalMs", 5 * 60 * 1000).get() / 1000;
    }

    @Override
    public String getProxyHost() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.proxyHost", null).get();
    }

    @Override
    public String getProxyPort() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.proxyPort", null).get();
    }

    @Override
    public String getProxyUserName() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.proxyUserName", null).get();
    }

    @Override
    public String getProxyPassword() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.proxyPassword", null).get();
    }


    @Override
    public boolean shouldGZipContent() {
        return configInstance.getBooleanProperty(
                namespace + "eurekaServer.gzipContent", true).get();
    }


    @Override
    public int getEurekaServerReadTimeoutSeconds() {
        return configInstance.getIntProperty(
                namespace + "eurekaServer.readTimeout", 8).get();
    }


    @Override
    public int getEurekaServerConnectTimeoutSeconds() {
        return configInstance.getIntProperty(
                namespace + "eurekaServer.connectTimeout", 5).get();
    }


    @Override
    public String getBackupRegistryImpl() {
        return configInstance.getStringProperty(namespace + "backupregistry",
                null).get();
    }


    @Override
    public int getEurekaServerTotalConnections() {
        return configInstance.getIntProperty(
                namespace + "eurekaServer.maxTotalConnections", 200).get();
    }

    @Override
    public int getEurekaServerTotalConnectionsPerHost() {
        return configInstance.getIntProperty(
                namespace + "eurekaServer.maxConnectionsPerHost", 50).get();
    }


    @Override
    public String getEurekaServerURLContext() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.context",
                configInstance.getStringProperty(namespace + "context", null)
                        .get()).get();
    }


    @Override
    public String getEurekaServerPort() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.port",
                configInstance.getStringProperty(namespace + "port", null)
                        .get()).get();
    }


    @Override
    public String getEurekaServerDNSName() {
        return configInstance.getStringProperty(
                namespace + "eurekaServer.domainName",
                configInstance
                        .getStringProperty(namespace + "domainName", null)
                        .get()).get();
    }


    @Override
    public boolean shouldUseDnsForFetchingServiceUrls() {
        return configInstance.getBooleanProperty(namespace + "shouldUseDns",
                false).get();
    }

    @Override
    public boolean shouldRegisterWithEureka() {
        return configInstance.getBooleanProperty(
                namespace + "registration.enabled", true).get();
    }

    @Override
    public boolean shouldUnregisterOnShutdown() {
        return configInstance.getBooleanProperty(
                namespace + "shouldUnregisterOnShutdown", true).get();
    }

    @Override
    public boolean shouldPreferSameZoneEureka() {
        return configInstance.getBooleanProperty(namespace + "preferSameZone",
                true).get();
    }

    @Override
    public boolean allowRedirects() {
        return configInstance.getBooleanProperty(namespace + "allowRedirects", false).get();
    }


    @Override
    public boolean shouldLogDeltaDiff() {
        return configInstance.getBooleanProperty(
                namespace + "printDeltaFullDiff", false).get();
    }

    @Override
    public boolean shouldDisableDelta() {
        return configInstance.getBooleanProperty(namespace + "disableDelta",
                false).get();
    }

    @Nullable
    @Override
    public String fetchRegistryForRemoteRegions() {
        return configInstance.getStringProperty(namespace + "fetchRemoteRegionsRegistry", null).get();
    }

    @Override
    public String getRegion() {
        DynamicStringProperty defaultEurekaRegion = configInstance.getStringProperty("eureka.region", "us-east-1");
        return configInstance.getStringProperty(namespace + "region", defaultEurekaRegion.get()).get();
    }


    @Override
    public String[] getAvailabilityZones(String region) {
        return configInstance
                .getStringProperty(
                        namespace + region + "." + "availabilityZones",
                        DEFAULT_ZONE).get().split(URL_SEPARATOR);
    }

    @Override
    public List<String> getEurekaServerServiceUrls(String myZone) {
        String serviceUrls = configInstance.getStringProperty(
                namespace + "serviceUrl" + "." + myZone, null).get();
        if (serviceUrls == null || serviceUrls.isEmpty()) {
            serviceUrls = configInstance.getStringProperty(
                    namespace + "serviceUrl" + ".default", null).get();

        }
        if (serviceUrls != null) {
            return Arrays.asList(serviceUrls.split(URL_SEPARATOR));
        }

        return new ArrayList<String>();
    }

    @Override
    public boolean shouldFilterOnlyUpInstances() {
        return configInstance.getBooleanProperty(
                namespace + "shouldFilterOnlyUpInstances", true).get();
    }


    @Override
    public int getEurekaConnectionIdleTimeoutSeconds() {
        return configInstance.getIntProperty(
                namespace + "eurekaserver.connectionIdleTimeoutInSeconds", 45)
                .get();
    }

    @Override
    public boolean shouldFetchRegistry() {
        return configInstance.getBooleanProperty(
                namespace + "shouldFetchRegistry", true).get();
    }

    @Override
    public boolean shouldEnforceFetchRegistryAtInit() {
        return configInstance.getBooleanProperty(
                namespace + "shouldEnforceFetchRegistryAtInit", false).get();
    }

    @Override
    public String getRegistryRefreshSingleVipAddress() {
        return configInstance.getStringProperty(
                namespace + "registryRefreshSingleVipAddress", null).get();
    }

    @Override
    public int getHeartbeatExecutorThreadPoolSize() {
        return configInstance.getIntProperty(
                namespace + "client.heartbeat.threadPoolSize", 5).get();
    }

    @Override
    public int getHeartbeatExecutorExponentialBackOffBound() {
        return configInstance.getIntProperty(
                namespace + "client.heartbeat.exponentialBackOffBound", 10).get();
    }

    @Override
    public int getCacheRefreshExecutorThreadPoolSize() {
        return configInstance.getIntProperty(
                namespace + "client.cacheRefresh.threadPoolSize", 5).get();
    }

    @Override
    public int getCacheRefreshExecutorExponentialBackOffBound() {
        return configInstance.getIntProperty(
                namespace + "client.cacheRefresh.exponentialBackOffBound", 10).get();
    }

    @Override
    public String getDollarReplacement() {
        return configInstance.getStringProperty(
                namespace + "dollarReplacement", "_-").get();
    }

    @Override
    public String getEscapeCharReplacement() {
        return configInstance.getStringProperty(
                namespace + "escapeCharReplacement", "__").get();
    }

    @Override
    public boolean shouldOnDemandUpdateStatusChange() {
        return configInstance.getBooleanProperty(
                namespace + "shouldOnDemandUpdateStatusChange", true).get();
    }

    @Override
    public boolean shouldEnforceRegistrationAtInit() {
        return configInstance.getBooleanProperty(
                namespace + "shouldEnforceRegistrationAtInit", false).get();
    }

    @Override
    public String getEncoderName() {
        return configInstance.getStringProperty(
                namespace + "encoderName", null).get();
    }

    @Override
    public String getDecoderName() {
        return configInstance.getStringProperty(
                namespace + "decoderName", null).get();
    }

    @Override
    public String getClientDataAccept() {
        return configInstance.getStringProperty(
                namespace + "clientDataAccept", EurekaAccept.full.name()).get();
    }

    @Override
    public String getExperimental(String name) {
        return configInstance.getStringProperty(namespace + "experimental" + "." + name, null).get();
    }

    @Override
    public EurekaTransportConfig getTransportConfig() {
        return transportConfig;
    }
}
