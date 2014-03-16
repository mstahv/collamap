package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.LineString;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class LineFeature extends SpatialFeature {

	public LineString getLine() {
		return (LineString) getGeom();
	}

	public void setLine(LineString route) {
		setGeom(route);
	}

}