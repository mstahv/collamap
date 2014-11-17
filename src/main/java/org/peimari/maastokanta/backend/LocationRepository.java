package org.peimari.maastokanta.backend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.peimari.maastokanta.domain.Location;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class LocationRepository {
    Map<String,Map<String,Location>> data = new HashMap<>();
    
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
    
    

}
