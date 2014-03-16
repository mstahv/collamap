package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.LinearRing;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class AreaFeature extends SpatialFeature {

	public LinearRing getArea() {
		return (LinearRing) getGeom();
	}

	public void setArea(LinearRing route) {
		setGeom(route);
	}

}