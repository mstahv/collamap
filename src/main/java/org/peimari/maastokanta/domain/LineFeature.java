package org.peimari.maastokanta.domain;

import javax.persistence.Entity;

import com.vividsolutions.jts.geom.LineString;

@Entity
public class LineFeature extends SpatialFeature {

	public LineString getLine() {
		return (LineString) getGeom();
	}

	public void setLine(LineString route) {
		setGeom(route);
	}

}