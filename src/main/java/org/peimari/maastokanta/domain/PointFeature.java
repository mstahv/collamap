package org.peimari.maastokanta.domain;


import com.vividsolutions.jts.geom.Point;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class PointFeature extends SpatialFeature {

	public Point getLocation() {
		return (Point) getGeom();
	}

	public void setLocation(Point location) {
		setGeom(location);
	}
	
}