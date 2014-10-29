package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.LinearRing;

public class AreaFeature extends SpatialFeature {

    static final long serialVersionUID = 1L;

    public LinearRing getArea() {
        return (LinearRing) getGeom();
    }

    public void setArea(LinearRing route) {
        setGeom(route);
    }

}
