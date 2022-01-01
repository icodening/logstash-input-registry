package cn.icodening.logstash.plugin.input.registry;

import java.util.EventObject;
import java.util.List;

public class NamingEvent extends EventObject {

    private final EventType eventType;

    public NamingEvent(List<ServiceInstance> source, EventType eventType) {
        super(source);
        this.eventType = eventType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceInstance> getSource() {
        return (List<ServiceInstance>) super.getSource();
    }

    public EventType eventType() {
        return eventType;
    }
}
