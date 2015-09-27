package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Point;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.peimari.maastokanta.tk102support.Update;
import org.vaadin.addon.leaflet.util.JTSUtil;

/**
 *
 * @author Matti Tahvonen
 */
public class LocationWithTail extends Location {

    private static final int MAX_POINTS = 200;

    private final LinkedList<Point> tail = new LinkedList<>();

    private Update lastUpdate;

    public LocationWithTail(String name, Update u) {
        super(name, getPoint(u), u.getTimestamp().toInstant(), 20);
        lastUpdate = u;
    }

    public void update(Update u) {
        if (u.getTimestamp().toInstant().isAfter(Instant.now().minusSeconds(
                120)) || getInstant().isAfter(u.getTimestamp().toInstant())) {
            Logger.getLogger(LocationWithTail.class.getName()).
                    log(Level.WARNING, "Old update!! " + u.toString());
        }
        tail.add(getPoint());
        if (tail.size() > MAX_POINTS) {
            tail.removeFirst();
        }
        setPoint(getPoint(u));
        setInstant(u.getTimestamp().toInstant());
        setAccuracy(20);
        setLastUpdate(u);
    }

    public List<Point> getTail() {
        return Collections.unmodifiableList(tail);
    }

    private static Point getPoint(Update u) {
        return JTSUtil.toPoint(
                new org.vaadin.addon.leaflet.shared.Point(u.getLat(), u.getLon()));
    }

    private void setLastUpdate(Update u) {
        lastUpdate = u;
    }

    public Update getLastUpdate() {
        return lastUpdate;
    }

}
