package cn.icodening.logstash.plugin.input.registry.nacos;

import cn.icodening.logstash.plugin.input.registry.RegistryService;
import cn.icodening.logstash.plugin.input.registry.RegistryServiceFactory;
import co.elastic.logstash.api.Configuration;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Map;
import java.util.Properties;

public class NacosRegistryServiceFactory implements RegistryServiceFactory {

    @Override
    public RegistryService createRegistryService(Configuration configuration) {
        Map<String, Object> nacosConfig = configuration.get(NacosPluginConfigSpecDefiner.NACOS_CONFIG);
        Properties properties = new Properties();
        properties.putAll(nacosConfig);
        try {
            NamingService namingService = NamingFactory.createNamingService(properties);
            return new NacosRegistryService(namingService);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
