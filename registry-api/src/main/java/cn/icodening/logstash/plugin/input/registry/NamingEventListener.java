package cn.icodening.logstash.plugin.input.registry;

import java.util.EventListener;

public interface NamingEventListener extends EventListener {

    void onEvent(NamingEvent event);
}
