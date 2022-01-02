package cn.icodening.logstash.plugin.input.registry;

import co.elastic.logstash.api.Configuration;

public interface RegistryServiceFactory {

    RegistryService createRegistryService(Configuration configuration);
}
