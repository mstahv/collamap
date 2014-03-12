package org.peimari.maastokanta.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class UserGroup extends AbstractEntity {

    private String name;
    
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private List<SpatialFeature> features = new ArrayList<>();
    
    @OneToMany(mappedBy = "group")
    @Cascade(CascadeType.ALL)
    private List<Style> styles = new ArrayList<>();
    
    @ManyToOne
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
