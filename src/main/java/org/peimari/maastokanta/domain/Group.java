package org.peimari.maastokanta.domain;

import javax.persistence.Entity;

@Entity
public class Group extends AbstractEntity {

	private String name;

	public Group() {
	}

	public Group(String name) {
		this.name = name;
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
