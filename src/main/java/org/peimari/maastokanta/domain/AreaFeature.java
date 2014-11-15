package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class AreaFeature extends SpatialFeature {

    static final long serialVersionUID = 1L;

    public Polygon getArea() {
        Geometry geom = getGeom();
        if (geom instanceof LinearRing) {
            // simple backwards compatibility trick
            LinearRing lr = (LinearRing) geom;
            GeometryFactory gf = new GeometryFactory();
            geom = gf.createPolygon(lr);
        }
        return (Polygon) geom;
    }

    public void setArea(Polygon route) {
        setGeom(route);
    }

}
