package org.peimari.maastokanta.domain;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class UserGroup extends AbstractEntity {

    private String name;
    
    @RelatedTo(type = "group")
    private List<SpatialFeature> features = new ArrayList<>();
    
    @RelatedTo(type = "group")
    private List<Style> styles = new ArrayList<>();
    
    @RelatedTo(direction = Direction.OUTGOING)
    @NotNull
    private Person admin;

    public UserGroup() {
    }

    public UserGroup(String name) {
        this.name = name;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public Person getAdmin() {
        return admin;
    }
    
    public void setAdmin(Person admin) {
        this.admin = admin;
    }
    
    public String getName() {
        return name;
    }
    
    public List<SpatialFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<SpatialFeature> features) {
        this.features = features;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Style[name:" + name + "]";
    }

    public Style addStyle(String name, String color) {
        return new Style(name, color, this);
    }

}
