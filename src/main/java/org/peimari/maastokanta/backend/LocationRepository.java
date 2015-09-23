package org.peimari.maastokanta.backend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.peimari.maastokanta.domain.DeviceMapping;
import org.peimari.maastokanta.domain.Location;
import org.peimari.maastokanta.domain.LocationWithTail;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class LocationRepository {
    
    Map<String,DeviceMapping> imeiToDeviceMapping = new HashMap<>();
    
    Map<String,Map<String,Location>> data = new HashMap<>();
    
    public void saveDeviceMapping(DeviceMapping deviceMapping) {
        imeiToDeviceMapping.put(deviceMapping.getImei(), deviceMapping);
    }
    
    public void saveLocation(String group, Location l) {
        getGroupData(group).put(l.getName(), l);
    }
    
    public List<Location> getLocations(String group) {
        ArrayList<Location> l = new ArrayList<>();
        Iterator<Location> iterator = getGroupData(group).values().iterator();
        final Instant hourAgo = Instant.now().minusSeconds(60*60);
        while(iterator.hasNext()) {
            Location next = iterator.next();
            if(next.getInstant().isBefore(hourAgo)) {
                iterator.remove();
            } else {
                l.add(next);
            }
        }
        return l;
    }
    
    private Map<String,Location> getGroupData(String group) {
        Map<String, Location> map = data.get(group);
        if(map == null) {
            map = new HashMap<>();
            data.put(group, map);
        }
        return map;
    }

    public DeviceMapping getDeviceMapping(String imei) {
        return imeiToDeviceMapping.get(imei);
    }

    public void saveLocationWithTail(String group, Location location) {
        Map<String, Location> groupData = getGroupData(group);
        Location oldLoc = groupData.get(location.getName());
        if(oldLoc != null && oldLoc instanceof LocationWithTail) {
            LocationWithTail locationWithTail = (LocationWithTail) oldLoc;
            locationWithTail.update(oldLoc);
        } else {
            groupData.put(location.getName(), new LocationWithTail(location));
        }
    }

}
