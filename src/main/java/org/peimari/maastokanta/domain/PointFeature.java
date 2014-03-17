package org.peimari.maastokanta.domain;


import com.vividsolutions.jts.geom.Point;

public class PointFeature extends SpatialFeature {

	public Point getLocation() {
		return (Point) getGeom();
	}

	public void setLocation(Point location) {
		setGeom(location);
	}
	
}