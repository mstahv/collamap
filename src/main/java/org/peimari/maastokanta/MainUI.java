/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.List;
import org.peimari.maastokanta.backend.FeatureRepository;
import org.peimari.maastokanta.backend.StyleRepository;
import org.peimari.maastokanta.backend.TagRepository;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.PersonRepository;
import org.peimari.maastokanta.domain.AreaFeature;
import org.peimari.maastokanta.domain.LineFeature;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.PointFeature;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.vaadin.addon.leaflet.AbstractLeafletLayer;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.fields.MTable;
import org.vaadin.maddon.layouts.MHorizontalLayout;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.VaadinUI;

/**
 *
 * @author mattitahvonenitmill
 */
@VaadinUI
@EnableAutoConfiguration
@Widgetset("org.peimari.maastokanta.AppWidgetSet")
public class MainUI extends UI implements Button.ClickListener, Window.CloseListener {

    @Autowired
    FeatureRepository repo;
    @Autowired
    TagRepository tags;
    @Autowired
    StyleRepository styles;
    @Autowired
    PersonRepository personRepository;

    private MTable<SpatialFeature> table = new MTable().withFullWidth().withProperties("id", "title", "Actions");
    private Button addNew = new MButton(FontAwesome.MAP_MARKER, this);
    private Button addNewWithRoute = new MButton(FontAwesome.MINUS, this);
    private Button addNewWithArea = new MButton(FontAwesome.SQUARE, this);
    private LMap map = new LMap();
    private LTileLayer osmTiles = new LTileLayer(
            "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
    LTileLayer peruskartta = new LTileLayer(
            "http://v3.tahvonen.fi/mvm71/tiles/peruskartta/{z}/{x}/{y}.png");

    @Autowired
    FeatureEditor editor;

    @Autowired
    AppService userService;

    private static boolean AUTOLOGIN_FOR_DEVELOPMENT = true;

    @Override
    protected void init(VaadinRequest request) {
        if (AUTOLOGIN_FOR_DEVELOPMENT && userService.getPerson() == null) {
            Person person = personRepository.findOne("matti@vaadin.com");
            if (person == null) {
                person = new Person();
                person.setDisplayName("Matti Tahvonen");
                person.setEmail("matti@vaadin.com");
                person = personRepository.save(person);
            }
            userService.setPerson(person);
        }

        if (!userService.isAuthtenticated()) {
            Page.getCurrent().setLocation(request.getContextPath() + "/auth");
            return;
        }

        Page.getCurrent().setTitle("Collamap: " + userService.getGroup().getName());

        table.addGeneratedColumn("Actions", new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, final Object se,
                    Object columnId) {
                final SpatialFeature feature = (SpatialFeature) se;
                Button edit = new Button("Edit", new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        editor.init(feature);
                    }
                });

                Button delete = new Button("Delete", new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        repo.delete(feature);
                        table.removeItem(feature);
                    }
                });
                return new MHorizontalLayout(edit, delete);
            }
        });

        loadEvents();

        osmTiles.setAttributionString("© OpenStreetMap Contributors");

        peruskartta.setAttributionString("Peruskartta: ©Maanmittauslaitos");
        peruskartta.setMaxZoom(18);
        peruskartta.setDetectRetina(true);

        HorizontalLayout actions = new MHorizontalLayout(addNew, addNewWithRoute, addNewWithArea);
        VerticalLayout layout = new MVerticalLayout(actions, map, table).expand(map, table);
        table.setSizeFull();
        layout.setSizeFull();
        setContent(layout);

        editor.addCloseListener(this);

    }

    private void loadEvents() {
        final List<SpatialFeature> events = repo.findByGroup(userService.getGroup());
        table.setBeans(events);

        /* ... and map */
        map.removeAllComponents();
        map.addBaseLayer(peruskartta, "PK");
        for (final SpatialFeature spatialEvent : events) {
            if (spatialEvent.getGeom() != null) {
                /* 
                 * JTSUtil wil make LMarker for point event, 
                 * LPolyline for events with route 
                 */
                AbstractLeafletLayer layer = (AbstractLeafletLayer) JTSUtil.toLayer(spatialEvent.getGeom());

                /* Add click listener to open event editor */
                layer.addClickListener(new LeafletClickListener() {

                    @Override
                    public void onClick(LeafletClickEvent e) {
                        editor.init(spatialEvent);
                    }
                });
                map.addLayer(layer);
            }
        }
        if (events.isEmpty()) {
            map.setCenter(61, 22);
            map.setZoomLevel(16);
        } else {
            map.zoomToContent();
        }

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
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
        newFeature.setGroup(userService.getGroup());
        editor.init(newFeature);
        editor.setCenterAndZoom(map.getCenter(), map.getZoomLevel());
    }

    @Override
    public void windowClose(Window.CloseEvent e) {
        // refresh table after edit
        loadEvents();
    }

}