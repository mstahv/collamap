package org.peimari.maastokanta.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Style extends AbstractEntity {

	private String name;
	
	@Version
	private Long version;

	public Style() {
	}

	public Style(String name) {
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
