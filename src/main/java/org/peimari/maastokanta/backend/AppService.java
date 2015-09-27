/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.backend;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.StringBuilderWriter;
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
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.LocationSettings;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "session")
public class AppService {

    private static CoordinateReferenceSystem TM35_CRS;
    private static CoordinateReferenceSystem GPS_CRS;

    static {
        try {
            TM35_CRS = CRS.decode("EPSG:3067");
            GPS_CRS = CRS.decode("EPSG:4326");
        } catch (FactoryException ex) {
            Logger.getLogger(AppService.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    @Autowired
    Repository repo;

    @Autowired
    Environment env;

    private Person person;
    private UserGroup group;
    private CoordinateReferenceSystem dataCRS;
    private MathTransform transform;
    
    private LocationSettings locationSettings;

    public LocationSettings getLocationSettings() {
        if(locationSettings == null) {
            locationSettings = new LocationSettings();
        }
        return locationSettings;
    }

    public boolean isAuthtenticated() {
        return group != null;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setGroup(UserGroup g) {
        this.group = g;
    }

    public UserGroup getGroup() {
        return group;
    }

    public UserGroup createNewGroup(String groupName) {
        UserGroup group = new UserGroup();
        group.setName(groupName);
        group.addStyle("Normal", "blue");
        group.addStyle("Important", "red");
        getPerson().addGroup(group.getId(), groupName);
        group.getEditorEmails().add(person.getEmail());
        repo.saveUsers();
        repo.persist(group);
        return group;
    }

    public boolean isDevMode() {
        return env.getProperty("devmode", "false").equals("true");
    }

    public Geometry getGeometryInGPSCoordinates(Feature f) {
        Geometry g = (Geometry) f.getDefaultGeometryProperty().getValue();
        try {
            g = JTS.transform(g, transform);
            return g;
        } catch (MismatchedDimensionException | TransformException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Feature> findByKiinteistotunnus(String needle) {
        ArrayList<Feature> features = new ArrayList<>();
        if (needle.contains("-")) {
            needle = KtUtil.ktToLongForm(needle);
        }
        try {
            Map map = new HashMap();

            File file = new File(
                    env.getProperty("fileroot") + "/shp/availableshapes.shp");
            map.put("url", file.toURL());

            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURL());
            String typeName = dataStore.
                    getTypeNames()[0];

            SimpleFeatureSource source = dataStore.getFeatureSource(typeName);

            FeatureCollection collection = source.getFeatures();
            FeatureIterator iterator = collection.features();

            dataCRS = source.getSchema().
                    getCoordinateReferenceSystem();
            CoordinateReferenceSystem target = CRS.decode("EPSG:4326");
            transform = CRS.findMathTransform(dataCRS, target, true);
            try {
                while (iterator.hasNext()) {
                    try {
                        Feature feature = (Feature) iterator.next();
                        String pitkatunnus = feature.getProperty("TEKSTI").
                                getValue().toString();
                        if (pitkatunnus.equals(needle)) {
                            features.add(feature);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(UserGroup.class.getName()).
                                log(Level.SEVERE, null, e);
                    }
                }
            } catch (MismatchedDimensionException ex) {
                Logger.getLogger(UserGroup.class.getName()).
                        log(Level.SEVERE, null, ex);
            } finally {
                iterator.close();
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(UserGroup.class.getName()).log(Level.SEVERE, null,
                    ex);
        } catch (IOException | FactoryException ex) {
            Logger.getLogger(UserGroup.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        return features;
    }

    public Collection<Feature> getAvailableGeometries() {
        ArrayList<Feature> geom = new ArrayList<>();
        try {

            Map map = new HashMap();
            File file = new File(
                    env.getProperty("fileroot") + "/shp/availableshapes.shp");
            map.put("url", file.toURL());

            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURL());
            String typeName = dataStore.
                    getTypeNames()[0];

            SimpleFeatureSource source = dataStore.getFeatureSource(typeName);

            FeatureCollection collection = source.getFeatures();
            FeatureIterator iterator = collection.features();

            dataCRS = source.getSchema().
                    getCoordinateReferenceSystem();
            CoordinateReferenceSystem target = CRS.decode("EPSG:4326");
            transform = CRS.findMathTransform(dataCRS, target,
                    true);
            try {
                while (iterator.hasNext()) {
                    Feature feature = (Feature) iterator.next();
                    geom.add(feature);
                }
            } catch (MismatchedDimensionException ex) {
                Logger.getLogger(UserGroup.class.getName()).
                        log(Level.SEVERE, null, ex);
            } finally {
                iterator.close();
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(UserGroup.class.getName()).log(Level.SEVERE, null,
                    ex);
        } catch (IOException | FactoryException ex) {
            Logger.getLogger(UserGroup.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        return geom;
    }

    public String writeCsv() {
        try {
            WrapDynaClass dc = WrapDynaClass.createDynaClass(
                    SpatialFeature.class);
            List<String> propertyNames = Arrays.asList(dc.getDynaProperties()).
                    stream().map(p -> p.getName()).collect(Collectors.toList());
            CSVFormat format = CSVFormat.EXCEL.withHeader(propertyNames.toArray(
                    new String[0]));
            final StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
            try (CSVPrinter printer = format.print(stringBuilderWriter)) {
                List<SpatialFeature> features = group.getFeatures();
                for (SpatialFeature feature : features) {
                    WrapDynaBean db = new WrapDynaBean(feature, dc);
                    printer.printRecord(propertyNames.stream().map(p -> db.
                            get(p)).collect(
                                    Collectors.toList()));
                }
            }
            return stringBuilderWriter.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void importCsv() {

    }

    public String calculateSize(AreaFeature areaFeature) {
        Polygon area = areaFeature.getArea();
        try {
            MathTransform transform = CRS.findMathTransform(GPS_CRS, TM35_CRS,
                    true);
            Geometry tm35 = JTS.transform(areaFeature.getArea(), transform);
            double sm = tm35.getArea() / 10000.0;
            return String.format("%.2f ha", sm);
        } catch (Exception ex) {
            Logger.getLogger(AppService.class.getName()).log(Level.SEVERE, null,
                    ex);
        }

        return "--";
    }

    public void setLocationSettings(LocationSettings fromLs) {
        locationSettings = fromLs;
    }

}
