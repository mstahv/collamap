package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import javax.validation.constraints.Size;

public class SpatialFeature extends AbstractEntity {

    private Geometry geom;

    private Style style;

    private Collection<Tag> tags = new HashSet<Tag>();

    @Size(max = 255)
    private String title;

    @Size(max = 36000)
    private String description;

    private Date lastModified;

    private Long version;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        prepersist();

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
        prepersist();

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
        prepersist();
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
        prepersist();

    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

}
