package org.peimari.maastokanta.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
public class UserGroup extends AbstractEntity {

    private String name;
    
    @OneToMany(mappedBy = "group")
    private List<SpatialFeature> features = new ArrayList<>();
    
    @ManyToOne
    @NotNull
    private Person admin;

    public UserGroup() {
    }

    public UserGroup(String name) {
        this.name = name;
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

}
