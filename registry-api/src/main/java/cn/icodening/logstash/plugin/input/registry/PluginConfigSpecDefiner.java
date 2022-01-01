package cn.icodening.logstash.plugin.input.registry;

import co.elastic.logstash.api.PluginConfigSpec;

import java.util.List;

public interface PluginConfigSpecDefiner {

    List<PluginConfigSpec<?>> getPluginConfigSpecs();
}
