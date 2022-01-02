package cn.icodening.logstash.plugin.input.registry;

import co.elastic.logstash.api.PluginConfigSpec;

public interface PluginConfigSpecDefiner {

    PluginConfigSpec<?> getPluginConfigSpecSchema();
}
