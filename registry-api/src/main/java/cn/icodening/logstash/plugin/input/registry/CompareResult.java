package cn.icodening.logstash.plugin.input.registry;

import java.util.List;

public class CompareResult {

    private final List<ServiceInstance> addedList;
    private final List<ServiceInstance> changedList;
    private final List<ServiceInstance> removedList;

    public CompareResult(List<ServiceInstance> addedList, List<ServiceInstance> changedList, List<ServiceInstance> removedList) {
        this.addedList = addedList;
        this.changedList = changedList;
        this.removedList = removedList;
    }

    public List<ServiceInstance> getAddedList() {
        return addedList;
    }

    public List<ServiceInstance> getChangedList() {
        return changedList;
    }

    public List<ServiceInstance> getRemovedList() {
        return removedList;
    }
}
