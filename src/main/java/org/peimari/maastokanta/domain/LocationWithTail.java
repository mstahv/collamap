package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matti Tahvonen
 */
public class LocationWithTail extends Location {
    
    private static final int MAX_POINTS = 200;
    
    private final LinkedList<Point> tail = new LinkedList<>();

    public LocationWithTail(Location location) {
        super(location.getName(), location.getPoint(), location.getInstant(), location.getAccuracy());
    }
    
    public void update(Location loc) {
        tail.add(getPoint());
        if (tail.size() > MAX_POINTS) {
            tail.removeFirst();
        }
        setPoint(loc.getPoint());
        setInstant(loc.getInstant());
        setAccuracy(loc.getAccuracy());
    }

    public List<Point> getTail() {
        return Collections.unmodifiableList(tail);
    }
    
}
