package org.peimari.maastokanta.domain;

import javax.persistence.Entity;

import com.vividsolutions.jts.geom.Point;

@Entity
public class PointFeature extends SpatialFeature {

	public Point getLocation() {
		return (Point) getGeom();
	}

	public void setLocation(Point location) {
		setGeom(location);
	}
	
}