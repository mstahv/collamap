package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.LineString;

public class LineFeature extends SpatialFeature {

    static final long serialVersionUID = 1L;

    public LineString getLine() {
        return (LineString) getGeom();
    }

    public void setLine(LineString route) {
        setGeom(route);
    }

}
