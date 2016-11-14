package org.peimari.maastokanta.backend;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleMap;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import java.util.List;
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.UserGroup;

/**
 *
 * @author Matti Tahvonen
 */
public class KmlWriter {

    public static Kml writeAsKml(UserGroup g, Kml kml) {
        List<SpatialFeature> features = g.getFeatures();

        Folder folder;
        Style style;
        StyleMap map;
        if (kml == null) {
            kml = new Kml();
            folder = kml.createAndSetFolder().withName("Collamap points").
                    withOpen(true);
            style = folder.createAndAddStyle().withId("s");
            PolyStyle s = style.createAndSetPolyStyle().withColor("4d6dff83");
            map = folder.createAndAddStyleMap().withId("a");

            map.createAndAddPair().withKey(StyleState.NORMAL).withStyleUrl("#s");
            map.createAndAddPair().withKey(StyleState.HIGHLIGHT).withStyleUrl(
                    "#s");
        } else {
            folder = (Folder) kml.getFeature();
        }
        for (org.peimari.maastokanta.domain.Style s2 : g.getStyles()) {
            mapStyle(folder, s2);
        }

        for (SpatialFeature sf : features) {

            if (sf instanceof AreaFeature) {
                AreaFeature areaFeature = (AreaFeature) sf;

                Polygon area = areaFeature.getArea();
                if (area != null) {

                    final Placemark pm = folder.createAndAddPlacemark();
                    pm.setDescription(areaFeature.getTitle());

                    org.peimari.maastokanta.domain.Style s = areaFeature.
                            getStyle();
                    if (s == null) {
                        pm.setStyleUrl("#a");
                    } else {
                        pm.setStyleUrl("#" + s.getName());
                    }

                    final de.micromata.opengis.kml.v_2_2_0.Polygon poly = pm.
                            createAndSetPolygon();
                    final Boundary ob = poly.createAndSetOuterBoundaryIs();
                    LinearRing outer = ob.
                            createAndSetLinearRing();

                    LineString exteriorRing = area.getExteriorRing();
                    for (Coordinate c : exteriorRing.getCoordinates()) {
                        outer.addToCoordinates(c.x, c.y);
                    }
                }
            }

        }
        return kml;
    }

    private static void mapStyle(Folder folder,
            org.peimari.maastokanta.domain.Style s2) {
        Style style;
        StyleMap map;
        style = folder.createAndAddStyle().withId(s2.getName() + "-s");
        String color = s2.getColor();
        if(color.equals("red")) {
            color = "1e1400FF";
        } else if( color.equals("blue")) {
            color = "1eF00014";
        }
        style.createAndSetPolyStyle().withColor(color);
        map = folder.createAndAddStyleMap().withId(s2.getName());
        map.createAndAddPair().withKey(StyleState.NORMAL).withStyleUrl(
                "#" + s2.getName() + "-s");
        map.createAndAddPair().withKey(StyleState.HIGHLIGHT).
                withStyleUrl("#" + s2.getName() + "-s");
    }

}
