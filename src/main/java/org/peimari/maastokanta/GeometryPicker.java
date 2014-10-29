package org.peimari.maastokanta;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import java.util.Collection;
import java.util.LinkedList;
import org.opengis.feature.Feature;
import org.peimari.maastokanta.backend.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

/**
 *
 * @author mattitahvonenitmill
 */
@UIScope
@VaadinComponent
public class GeometryPicker extends Window {

    @Autowired
    AppService as;

    @Override
    public void attach() {
        super.attach(); //To change body of generated methods, choose Tools | Templates.
        setModal(true);
        setCaption("Choose geometry");
        setWidth(90, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        LMap map = new LMap();
        map.addLayer(new LOpenStreetMapLayer());
        Collection<Feature> geom = as.getAvailableGeometries();

        for (final Feature feature : geom) {
            Geometry geometry = as.getGeometryInGPSCoordinates(feature);
            LeafletLayer toLayer = JTSUtil.toLayer(DouglasPeuckerSimplifier.
                    simplify(geometry, 0.0005));
            if (toLayer instanceof LPolygon) {
                LPolygon lPolygon = (LPolygon) toLayer;
                lPolygon.addClickListener(e -> {
                    ((MainUI) getUI()).newFrom(feature, "");
                });
                map.addComponent(lPolygon);
            } else if (toLayer instanceof LLayerGroup) {
                LLayerGroup lLayerGroup = (LLayerGroup) toLayer;
                LinkedList<LPolygon> lp = new LinkedList<>();
                for (Component component : lLayerGroup) {
                    LPolygon lPolygon = (LPolygon) component;
                    lPolygon.addClickListener(e -> {
                        ((MainUI) getUI()).newFrom(feature, "");
                    });
                    lp.add(lPolygon);
                }
                for (LPolygon lPolygon : lp) {
                    map.addLayer(lPolygon);
                }
            }
        }
        map.zoomToContent();
        setContent(map);
    }

    void activate() {
        UI.getCurrent().addWindow(this);
    }
}
