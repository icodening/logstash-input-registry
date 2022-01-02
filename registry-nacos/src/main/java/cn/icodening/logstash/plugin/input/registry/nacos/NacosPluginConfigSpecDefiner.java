package cn.icodening.logstash.plugin.input.registry.nacos;

import cn.icodening.logstash.plugin.input.registry.PluginConfigSpecDefiner;
import co.elastic.logstash.api.PluginConfigSpec;

import java.util.Map;

public class NacosPluginConfigSpecDefiner implements PluginConfigSpecDefiner {

    public static final PluginConfigSpec<Map<String, Object>> NACOS_CONFIG = PluginConfigSpec.hashSetting("nacos");

    @Override
    public PluginConfigSpec<?> getPluginConfigSpecSchema() {
        return NACOS_CONFIG;
    }
}
