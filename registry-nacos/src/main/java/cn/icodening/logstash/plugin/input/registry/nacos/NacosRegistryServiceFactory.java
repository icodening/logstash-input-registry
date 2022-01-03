package cn.icodening.logstash.plugin.input.registry.nacos;

import cn.icodening.logstash.plugin.input.registry.RegistryService;
import cn.icodening.logstash.plugin.input.registry.RegistryServiceFactory;
import co.elastic.logstash.api.Configuration;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

public class NacosRegistryServiceFactory implements RegistryServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosRegistryServiceFactory.class);

    @Override
    public RegistryService createRegistryService(Configuration configuration) {
        Map<String, Object> config = configuration.get(NacosPluginConfigSpecDefiner.NACOS_CONFIG);
        if (config == null || config.isEmpty()) {
            LOGGER.info("no configuration for nacos");
            return null;
        }
        LOGGER.info("nacos config: {}", config);
        Properties properties = new Properties();
        properties.putAll(config);
        try {
            NamingService namingService = NamingFactory.createNamingService(properties);
            return new NacosRegistryService(namingService);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
