package org.peimari.maastokanta.domain;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Style extends AbstractEntity {

    private String name;
    private String color;
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
