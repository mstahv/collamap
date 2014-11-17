
package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;
import java.time.Instant;

/**
 *
 * @author matti ät vaadin.com
 */
public class Location implements Serializable {
    String name;
    Point point;
    Instant instant;
    double accuracy;

    public Location(String name, Point point, Instant instant, double accuracy) {
        this.name = name;
        this.point = point;
        this.instant = instant;
        this.accuracy = accuracy;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getName() {
        return name;
    }

    public Point getPoint() {
        return point;
    }
    
    public Instant getInstant() {
        return instant;
    }

}
