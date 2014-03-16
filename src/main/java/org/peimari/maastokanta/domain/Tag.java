package org.peimari.maastokanta.domain;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Tag extends AbstractEntity {

	private String name;
	
	@GraphId
	private Long version;

	public Tag() {
	}

	public Tag(String name) {
		this.name = name;
	}

	public Long getVersion() {
		return version;
	}

	protected void setVersion(Long version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Style[name:" + name + "]";
	}

}
