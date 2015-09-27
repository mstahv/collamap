package org.peimari.maastokanta.backend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.peimari.maastokanta.domain.DeviceMapping;
import org.peimari.maastokanta.domain.Location;
import org.peimari.maastokanta.domain.LocationWithTail;
import org.peimari.maastokanta.tk102support.Update;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class LocationRepository {

    private Map<String, DeviceMapping> imeiToDeviceMapping = new HashMap<>();

    private Map<String, Map<String, Location>> data = new HashMap<>();
    
    public LocationRepository() {
        Logger.getAnonymousLogger().warning("Creating location repo");
    }

    public void saveDeviceMapping(DeviceMapping deviceMapping) {
        imeiToDeviceMapping.put(deviceMapping.getImei(), deviceMapping);
    }

    public void saveDeviceMappings(List<DeviceMapping> mappings) {
        if (mappings != null) {
            for (DeviceMapping mapping : mappings) {
                imeiToDeviceMapping.put(mapping.getImei(), mapping);
            }
        }
    }

    public void saveLocation(String group, Location l) {
        getGroupData(group).put(l.getName(), l);
    }

    public List<Location> getLocations(String group) {
        ArrayList<Location> l = new ArrayList<>();
        Iterator<Location> iterator = getGroupData(group).values().iterator();
        final Instant hourAgo = Instant.now().minusSeconds(60 * 60);
        while (iterator.hasNext()) {
            Location next = iterator.next();
            if (next.getInstant().isBefore(hourAgo)) {
                iterator.remove();
            } else {
                l.add(next);
            }
        }
        return l;
    }

    private Map<String, Location> getGroupData(String group) {
        Map<String, Location> map = data.get(group);
        if (map == null) {
            map = new HashMap<>();
            data.put(group, map);
        }
        return map;
    }

    public DeviceMapping getDeviceMapping(String imei) {
        return imeiToDeviceMapping.get(imei);
    }

    public void saveLocationWithTail(String group, String name, Update update) {
        Map<String, Location> groupData = getGroupData(group);
        Logger.getAnonymousLogger().warning("Size " + groupData.size());
        Location oldLoc = groupData.get(name);
        if (oldLoc != null && oldLoc instanceof LocationWithTail) {
            LocationWithTail locationWithTail = (LocationWithTail) oldLoc;
            locationWithTail.update(update);
        } else {
            groupData.put(name, new LocationWithTail(name, update));
        }
        
    }

}
