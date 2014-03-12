package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class SpatialFeature extends AbstractEntity {

    @Column(name = "geometry", columnDefinition = "Geometry", nullable = true)
    private Geometry geom;

    @ManyToOne
    private Style style;

    @ManyToMany
    private Collection<Tag> tags = new HashSet<Tag>();

    @Size(max = 255)
    private String title;

    @Size(max = 36000)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @Version
    private Long version;

    @ManyToOne
    @NotNull
    private UserGroup group;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }

    public Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Feature[title:" + title + "]";
    }

    @PrePersist
    private void prepersist() {
        lastModified = new Date();
    }

    public SpatialFeature() {
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

}
