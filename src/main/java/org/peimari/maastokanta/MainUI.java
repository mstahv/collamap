/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.peimari.maastokanta.auth.LoginView;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.KtUtil;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.LineFeature;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.PointFeature;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.UserGroup;
import org.peimari.maastokanta.mobile.MobileUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.vaadin.addon.leaflet.AbstractLeafletLayer;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import org.vaadin.addon.leaflet.LPolygon;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LWmsLayer;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author mattitahvonenitmill
 */
@SpringUI(path = "/admin")
@EnableAutoConfiguration
@Widgetset("org.peimari.maastokanta.AppWidgetSet")
@Theme("valo")
public class MainUI extends UI implements Button.ClickListener,
        Window.CloseListener {

    @Autowired
    Repository repo;
    
    private MTable<SpatialFeature> table = new MTable().withFullWidth().
            withProperties("title", "Actions").withFullHeight().withFullWidth().
            expand("title");
    private Button addNew = toolButton(FontAwesome.MAP_MARKER).withDescription(
            "Add marker");
    private Button addNewWithRoute = toolButton(FontAwesome.MINUS).
            withDescription("Add path");
    private Button addNewWithArea = toolButton(FontAwesome.SQUARE).
            withDescription("Add area");
    private Button addFromAvailableGeometries = toolButton(FontAwesome.SQUARE_O).
            withCaption("...").withDescription("Add area from catalog");
    private Button addWithKiinteistotunnus = toolButton(FontAwesome.SEARCH_PLUS).
            withDescription("Add with kiinteistötunnus");
    private Button editStyles = toolButton(FontAwesome.ELLIPSIS_H).
            withDescription("Edit styles");
    private Button save = toolButton(FontAwesome.FLOPPY_O).withDescription(
            "Save to server");
    private Button exportCsv = toolButton(FontAwesome.DOWNLOAD).withDescription(
            "Export");
//    private Button importCsv = toolButton(FontAwesome.UPLOAD).withDescription(
//            "Import");
    private Component spacer = new Label("");
    private MTextField filter = new MTextField().withInputPrompt("filter...").
            withFullWidth();
    private Button logout = new MButton("logout", this);
    private Button chooseGroup = new MButton("Other group...", this);
    private LMap map = new LMap();
    private LOpenStreetMapLayer osmTiles = new LOpenStreetMapLayer();
    LTileLayer peruskartta = new LTileLayer(MobileUI.peruskarttaosoite);
    LWmsLayer mapant = new LWmsLayer();
//    LTileLayer peruskartta = new LTileLayer(
//            "https://wf.virit.in/mvm75/tiles/peruskartta/{z}/{x}/{y}.png");

    @Autowired
    FeatureEditor editor;

    @Autowired
    GeometryPicker picker;

    @Autowired
    AppService service;

    @Autowired
    PropertiesEditor styleEditor;
    
    @Autowired
    LoginView loginView;

    private UserGroup group;

    private final MButton toolButton(Resource icon) {
        return new MButton(icon, this).withStyleName(ValoTheme.BUTTON_ICON_ONLY);
    }

    @Override
    protected void init(VaadinRequest request) {
        if (service.isDevMode() && service.getPerson() == null) {
            Person person = repo.getPerson("matti@vaadin.com");
            if (person == null) {
                person = new Person();
                person.setDisplayName("Matti Tahvonen");
                person.setEmail("matti@vaadin.com");
                repo.persist(person);
            }
            service.setPerson(person);
        }
        
        if (!service.isAuthtenticated()) {
            setContent(loginView);
            return;
        }

        group = service.getGroup();

        Page.getCurrent().setTitle("Collamap: " + service.getGroup().getName());

        table.addGeneratedColumn("Actions", (source, se, columnId) -> {
            final SpatialFeature feature = (SpatialFeature) se;
            Button edit = new MButton(FontAwesome.EDIT, e -> editor.
                    init(feature));

            Button delete = new ConfirmButton(FontAwesome.TRASH_O,
                    "Are you really sure about this desctrutive operation??",
                    e -> {
                        group.getFeatures().remove(feature);
                        loadEvents(filter.getValue());
                    });
            edit.setStyleName(ValoTheme.BUTTON_SMALL);
            edit.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            delete.setStyleName(ValoTheme.BUTTON_SMALL);
            delete.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            return new MHorizontalLayout(edit, delete).withSpacing(false);
        });

        loadEvents(null);

        peruskartta.setAttributionString("Peruskartta: ©Maanmittauslaitos");
        peruskartta.setMaxZoom(18);
        peruskartta.setDetectRetina(true);
        
        mapant.setUrl("http://wmts.mapant.fi/wmts_EPSG3857.php?z={z}&x={x}&y={y}");
        mapant.setMaxZoom(19);
        mapant.setMinZoom(7);
        mapant.setAttributionString("<a href=\"http://www.maanmittauslaitos.fi/en/digituotteet/laser-scanning-data\" target=\"_blank\">Laser scanning</a> and <a href=\"http://www.maanmittauslaitos.fi/en/digituotteet/topographic-database\" target=\"_blank\">topographic</a> data provided by the <a href=\"http://www.maanmittauslaitos.fi/en\" target=\"_blank\">National Land Survey of Finland</a> under the <a href=\"https://creativecommons.org/licenses/by/4.0/legalcode\">Creative Commons license</a>.");


        HorizontalLayout actions = new MHorizontalLayout(addNew, addNewWithRoute,
                addNewWithArea, addFromAvailableGeometries,
                addWithKiinteistotunnus, editStyles, exportCsv, save,
                spacer,
                chooseGroup, logout).expand(spacer).withFullWidth();
        final MHorizontalLayout maincontent = new MHorizontalLayout(
                map,
                new MVerticalLayout(filter).expand(table)
                .withMargin(false).withWidth("300px")
        ).withFullWidth().withFullHeight().expand(map);
        VerticalLayout layout = new MVerticalLayout(actions).
                expand(maincontent).withFullHeight().withFullWidth();
        layout.setSizeFull();
        setContent(layout);
        
        filter.addTextChangeListener(e -> loadEvents(e.getText()));

        FileDownloader fileDownloader = new FileDownloader(new StreamResource(
                () -> {
                    return new ByteArrayInputStream(service.writeCsv().
                            getBytes());
                }, "export.csv"));
        fileDownloader.extend(exportCsv);

    }

    private void loadEvents(String filter) {
        final List<SpatialFeature> features;
        if (filter != null && !filter.isEmpty()) {
            features = group.getFeatures(filter);
        } else {
            features = group.getFeatures();
        }

        table.setBeans(features);

        /* ... and map */
        map.removeAllComponents();
        map.addBaseLayer(peruskartta, "Peruskartta");
        map.addBaseLayer(mapant, "MapAnt");
        // map.addBaseLayer(osmTiles, "OSM");
        for (final SpatialFeature spatialEvent : features) {
            if (spatialEvent.getGeom() != null) {
                /* 
                 * JTSUtil wil make LMarker for point event, 
                 * LPolyline for events with route 
                 */
                AbstractLeafletLayer layer = (AbstractLeafletLayer) JTSUtil.
                        toLayer(spatialEvent.getGeom());

                /* Add click listener to open event editor */
                layer.addClickListener(new LeafletClickListener() {

                    @Override
                    public void onClick(LeafletClickEvent e) {
                        editor.init(spatialEvent);
                    }
                });
                if (spatialEvent.getStyle() != null && spatialEvent.getStyle().
                        getColor() != null && (layer instanceof LPolygon)) {
                    LPolygon lPolygon = (LPolygon) layer;
                    lPolygon.setColor(spatialEvent.getStyle().getColor());
                }
                map.addLayer(layer);
            }
        }
        if (features.isEmpty()) {
            map.setCenter(60.4568759, 22.6870261);
            map.setZoomLevel(13);
        } else {
            map.zoomToContent();
        }

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == save) {
            repo.persist(group);
            Notification.show("Saved!");
            return;
        }
        if (event.getButton() == exportCsv) {
            return;
        }
        if (event.getButton() == logout) {
            service.setGroup(null);
            service.setPerson(null);
            Page.getCurrent().reload();
            return;
        }
        if (event.getButton() == chooseGroup) {
            service.setGroup(null);
            Page.getCurrent().reload();
            return;
        }
        if (event.getButton() == addWithKiinteistotunnus) {
            // TODO can haz prompt!?!
            Window window = new Window("Find with Kiinteistötunnus");
            MTextField tunnus = new MTextField().withInputPrompt(
                    "577-XXX-XXXX-XXXX");
            Button add = new PrimaryButton("Add", e -> {
                List<Feature> features = service.findByKiinteistotunnus(tunnus.getValue());
                if (!features.isEmpty()) {
                    int i = 0;
                    for (Feature f : features) {
                        i++;
                        String postfix = features.size() == 1 ? "" : "-"+i;
                        newFrom(f, postfix);
                    }
                    if(features.size() > 1) {
                        filter.setValue(KtUtil.ktToLongForm(tunnus.getValue()));
                        Notification.show("Found several shapes, filtered and editing one of them.");
                    }
                    window.close();
                } else {
                    Notification.show("Not found!",
                            Notification.Type.WARNING_MESSAGE);
                }
            });
            window.setContent(new MVerticalLayout(tunnus, add));
            window.setModal(true);
            addWindow(window);
            return;
        }

        if (event.getButton() == addFromAvailableGeometries) {
            picker.activate();
            return;
        } else if (event.getButton() == editStyles) {
            styleEditor.activate();
            return;
        }

        SpatialFeature newFeature;
        if (event.getButton() == addNew) {
            newFeature = new PointFeature();
        } else if (event.getButton() == addNewWithRoute) {
            newFeature = new LineFeature();
        } else if (event.getButton() == addNewWithArea) {
            newFeature = new AreaFeature();
        } else {
            throw new IllegalArgumentException();
        }
        editFeature(newFeature);
    }

    private void editFeature(SpatialFeature newFeature) {
        group.getFeatures().add(newFeature);
        FeatureEditor e = editor.init(newFeature);
        e.setCenterAndZoom(map.getCenter(), map.getZoomLevel());
    }

    @Override
    public void windowClose(Window.CloseEvent e) {
        // refresh table after edit
        loadEvents(filter.getValue());
    }

    void newFrom(Feature feature, String postfix) {
        Geometry geometry = service.getGeometryInGPSCoordinates(feature);
        // TODO support multipolygons in V-Leaflet
        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            AreaFeature areaFeature = new AreaFeature();
            final Polygon polygon = (Polygon) multiPolygon.getGeometryN(0);
            areaFeature.setGeom(polygon);
            Property property = feature.getProperty("TEKSTI");
            if (property != null) {
                areaFeature.setTitle(property.getValue().toString() + postfix);
            }
            editFeature(areaFeature);
        } else {
            throw new IllegalArgumentException("Unknown geometry type");
        }
    }

}
