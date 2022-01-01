package cn.icodening.logstash.plugin.input.registry;

import java.util.EventListener;

public interface RegistryEventListener extends EventListener {

    void onEvent(RegistryEvent event);
}
