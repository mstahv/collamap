package org.peimari.maastokanta.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserGroup extends AbstractEntity {
    
    private String id = UUID.randomUUID().toString();

    private String name;
    
    private List<SpatialFeature> features = new ArrayList<>();
    
    private List<Style> styles = new ArrayList<>();
    
    public UserGroup() {
    }

    public String getId() {
        return id;
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

    public void addStyle(String name, String color) {
        styles.add(new Style(name, color));
    }

}
