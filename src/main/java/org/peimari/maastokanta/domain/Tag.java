package org.peimari.maastokanta.domain;


public class Tag extends AbstractEntity {

	private String name;
	
	public Tag() {
	}

	public Tag(String name) {
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
