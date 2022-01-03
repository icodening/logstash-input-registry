package cn.icodening.logstash.plugin.input.registry.eureka;

import cn.icodening.logstash.plugin.input.registry.RegistryService;
import cn.icodening.logstash.plugin.input.registry.RegistryServiceFactory;
import co.elastic.logstash.api.Configuration;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EurekaRegistryServiceFactory implements RegistryServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurekaRegistryServiceFactory.class);

    @Override
    public RegistryService createRegistryService(Configuration configuration) {
        Map<String, Object> config = configuration.get(EurekaPluginConfigSpecDefiner.EUREKA_CONFIG);
        if (config == null || config.isEmpty()) {
            LOGGER.info("no configuration for eureka");
            return null;
        }
        LOGGER.info("eureka config: {}", config);
        try {
            MapConfiguration mapConfiguration = new MapConfiguration(config);
            DynamicPropertyFactory dynamicPropertyFactory = DynamicPropertyFactory.initWithConfigurationSource(mapConfiguration);

            InstanceConfiguration instanceConfig = new InstanceConfiguration(dynamicPropertyFactory);
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            ApplicationInfoManager applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);

            ClientConfiguration clientConfiguration = new ClientConfiguration(dynamicPropertyFactory);
            DiscoveryManager.getInstance().setEurekaClientConfig(clientConfiguration);
            DiscoveryClient discoveryClient = new DiscoveryClient(applicationInfoManager, clientConfiguration);
            return new EurekaRegistryService(discoveryClient);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
