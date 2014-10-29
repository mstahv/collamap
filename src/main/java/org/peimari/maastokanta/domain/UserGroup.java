package org.peimari.maastokanta.domain;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class UserGroup extends AbstractEntity {

    static final long serialVersionUID = 1L;

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
    
    public List<SpatialFeature> getFeatures(String filter) {
        return getFeatures().stream().filter(p->p.toString().toLowerCase()
                .contains(filter.toLowerCase())).collect(Collectors.toList());
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
