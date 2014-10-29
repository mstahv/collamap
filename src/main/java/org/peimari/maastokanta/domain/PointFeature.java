package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Point;

public class PointFeature extends SpatialFeature {

    static final long serialVersionUID = 1L;

    public Point getLocation() {
        return (Point) getGeom();
    }

    public void setLocation(Point location) {
        setGeom(location);
    }

}
