package cn.icodening.logstash.plugin.input.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicesComparator {

    private ServicesComparator() {
    }

    public static CompareResult compareServices(List<ServiceInstance> oldList, List<ServiceInstance> newList) {
        Map<String, ServiceInstance> newInstanceMap = new HashMap<>();
        for (ServiceInstance newInstance : newList) {
            newInstanceMap.put(newInstance.getServiceId() + "@" + newInstance.getHostPort(), newInstance);
        }
        List<ServiceInstance> temp = new ArrayList<>(oldList);
        temp.removeAll(newList);

        //find remove, change, add
        List<ServiceInstance> updateInstances = new ArrayList<>();
        List<ServiceInstance> removedInstances = new ArrayList<>();
        for (ServiceInstance next : temp) {
            String key = next.getServiceId() + "@" + next.getHostPort();
            ServiceInstance serviceInstance = newInstanceMap.remove(key);
            if (serviceInstance == null) {
                removedInstances.add(next);
                continue;
            }
            updateInstances.add(next);
        }
        List<ServiceInstance> addedInstances = new ArrayList<>(newInstanceMap.values());
        return new CompareResult(addedInstances, updateInstances, removedInstances);
    }
}
