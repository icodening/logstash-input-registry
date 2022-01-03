package cn.icodening.logstash.plugin.input.registry.eureka;

import cn.icodening.logstash.plugin.input.registry.PluginConfigSpecDefiner;
import co.elastic.logstash.api.PluginConfigSpec;

import java.util.Map;

public class EurekaPluginConfigSpecDefiner implements PluginConfigSpecDefiner {

    public static final PluginConfigSpec<Map<String,Object>> EUREKA_CONFIG= PluginConfigSpec.hashSetting("eureka");

    @Override
    public PluginConfigSpec<?> getPluginConfigSpecSchema() {
        return EUREKA_CONFIG;
    }
}
