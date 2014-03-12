package org.peimari.maastokanta.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Style extends AbstractEntity {

    private String name;
    private String color;

    @Version
    private Long version;

    @ManyToOne
    private UserGroup group;

    public Style() {
    }
    
    Style(String name, String color, UserGroup group) {
        this.name = name;
        this.color = color;
        this.group = group;
        group.getStyles().add(this);
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
