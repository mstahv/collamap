package org.peimari.maastokanta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.touchkit.extensions.Geolocator;
import com.vaadin.addon.touchkit.extensions.LocalStorage;
import com.vaadin.addon.touchkit.extensions.LocalStorageCallback;
import com.vaadin.addon.touchkit.extensions.PositionCallback;
import com.vaadin.addon.touchkit.gwt.client.vcom.Position;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vividsolutions.jts.geom.LineString;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.LocationRepository;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.DeviceMapping;
import org.peimari.maastokanta.domain.Location;
import org.peimari.maastokanta.domain.LocationSettings;
import org.peimari.maastokanta.domain.LocationWithTail;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.Style;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.vaadin.addon.leaflet.AbstractLeafletVector;
import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LFeatureGroup;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.spring.touchkit.TouchKitUI;
import org.vaadin.viritin.MBeanFieldGroup;
import org.vaadin.viritin.fields.ElementCollectionField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author Matti Tahvonen <matti@vaadin.com>
 */
@TouchKitUI()
@EnableAutoConfiguration
@Widgetset("org.peimari.maastokanta.MobileAppWidgetSet")
@Theme("touchkit")
@Title("Collamap")
public class MobileUI extends UI {

    @Autowired
    AppService service;

    @Autowired
    Repository repo;

    @Autowired
    LocationRepository locationRepository;

    Map<UserGroup, LLayerGroup> grouptToLayers = new HashMap<>();

    NavigationView mapView = new NavigationView("Collamap");
    NavigationView settingsView = new NavigationView("Settings");
    NavigationManager navman = new NavigationManager(mapView);
    LMap map = new LMap();

    Button locate = new Button(FontAwesome.BULLSEYE);

    LLayerGroup others = new LFeatureGroup();

    LCircle me;

    MTextField userName = new MTextField("Username");
    MTextField group = new MTextField("Sharing group");
    private boolean centerNextLocation = true;

    public static class DeviceMappingFields {

        public MTextField imei = new MTextField().withFullWidth();
        public MTextField name = new MTextField().withWidth("40px");
        public MTextField group = new MTextField().withWidth("40px");
        public Switch enabled = new Switch("");

    }

    ElementCollectionField<DeviceMapping> deviceMappings = new ElementCollectionField<>(
            DeviceMapping.class, DeviceMappingFields.class).withCaption(
            "Dogs").expand("imei").withFullWidth().setAllowRemovingItems(false);

    Switch locationSharing = new Switch("Share location");

    TypedSelect<Integer> trackingInterval = new TypedSelect(
            "Tracking interval(s)").setOptions(5, 10, 30).withFullWidth();

    int amountOfRepeatedPositionRequests = 0;
    private static final int MAX_GEOLOC_CHECKS = 30;

    private PositionCallback positionCallBack = new PositionCallback() {

        @Override
        public void onSuccess(Position position) {
            amountOfRepeatedPositionRequests++;
            double accuracy = position.getAccuracy();
            if (amountOfRepeatedPositionRequests < MAX_GEOLOC_CHECKS && accuracy > 50) {
                Geolocator.detect(positionCallBack);
                return;
            }
            final Point myloc = new Point(position.getLatitude(),
                    position.getLongitude());
            if (accuracy > 50) {
                Notification.show(
                        "Accurasy is pretty weak, only " + accuracy + " meters. Relocating again in couple of seconds might help.");
            }
            if (accuracy < 15) {
                accuracy = 15;
            }
            if (me == null) {
                me = new LCircle(myloc, accuracy);
                me.setColor("red");
                map.addComponent(me);
            } else {
                me.setPoint(myloc);
                me.setRadius(accuracy);
            }
            if(centerNextLocation) {
                double zoomlevel = findAppropriateZoomlevel(accuracy);
                map.setCenter(myloc, zoomlevel);
                centerNextLocation = false;
           }
            locate.setEnabled(true);

            others.removeAllComponents();
            if (service.getLocationSettings().isLocationSharing()) {
                final String myName = service.getLocationSettings().
                        getUserName();
                Location location = new Location(myName, JTSUtil.toPoint(myloc),
                        Instant.now(), position.getAccuracy());
                final String groupName = service.getLocationSettings().
                        getGroup();
                locationRepository.saveLocation(groupName, location);
                Instant now = Instant.now();
                List<Location> locations = locationRepository.getLocations(
                        groupName);
                for (Location l : locations) {
                    if (!l.getName().equals(myName)) {

                        LMarker lMarker = new LMarker(l.getPoint());
                        long secondsAgo = now.getEpochSecond() - l.
                                getInstant().
                                getEpochSecond();
                        long minutesAgo = secondsAgo / 60;
                        lMarker.setPopup(
                                l.getName() + ", " + minutesAgo + "' ago, " + l.
                                getAccuracy() + "m accuracy");
                        others.addComponent(lMarker);
                        if (l instanceof LocationWithTail) {
                            LocationWithTail locationWithTail = (LocationWithTail) l;

                            com.vividsolutions.jts.geom.Point lastpoint = locationWithTail.
                                    getPoint();
                            List<Point> points = locationWithTail.getTail().
                                    stream().map(p -> new Point(p.getY(), p.
                                            getX())).collect(
                                    Collectors.toList());
                            points.add(new Point(lastpoint.getY(), lastpoint.
                                    getX()));
                            LPolyline tail = new LPolyline(points.toArray(
                                    new Point[points.size()]));
                            others.addComponent(tail);
                        }

                    }
                }
                if (others.getParent() == null) {
                    map.addComponent(others);
                }
                if(trackingInterval.getValue() != null) {
                    setPollInterval(trackingInterval.getValue()*1000);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(trackingInterval.getValue()*1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(MobileUI.class.getName()).
                                        log(Level.SEVERE, null, ex);
                            }
                            access(() -> {
                                Geolocator.detect(positionCallBack);
                            });
                        }
                    }.start();
                            
                }
            }
        }

        @Override
        public void onFailure(int errorCode) {
            Notification.show("Geolocation request failed " + errorCode,
                    Notification.Type.ERROR_MESSAGE);
            locate.setEnabled(true);
        }

        private double findAppropriateZoomlevel(double accuracy) {
            final int screenW = MobileUI.this.getPage().
                    getBrowserWindowWidth();
            double[] mPerPixel = new double[]{156412, 78206, 39103, 19551, 9776, 4888, 2444, 1222, 610.984, 305.492, 152.746, 76.373, 38.187, 19.093, 9.547, 4.773, 2.387, 1.193, 0.596, 0.298};
            int zoomLevel = 0;
            for (int i = 0; i < mPerPixel.length; i++) {
                double metersOnScreen = mPerPixel[i] * screenW;
                if (metersOnScreen / 15.0 > accuracy) {
                    zoomLevel = i;
                } else {
                    break;
                }
            }
            if (zoomLevel > 15) {
                zoomLevel = 15;
            }
            return zoomLevel;
        }
    };

    @Override
    protected void init(VaadinRequest request) {
        initMap();
        initSettingsView();
        mapView.setSizeFull();
        mapView.setContent(map);
        final NavigationButton settingsButton = new NavigationButton(
                "\u00A0\u00A0\u00A0");
        settingsButton.setPrimaryStyleName("v-button");
        settingsButton.setIcon(FontAwesome.NAVICON);
        settingsButton.addClickListener(e -> {
            navman.navigateTo(settingsView);
        });
        mapView.setRightComponent(settingsButton);
        navman.setNextComponent(settingsView);
        setContent(navman);

        LocalStorage.get().get("layers", new LocalStorageCallback() {

            @Override
            public void onSuccess(String value) {
                if (value != null) {
                    for (String id : value.split(",")) {
                        addGroup(repo.getGroup(id));
                    }
                }
            }

            @Override
            public void onFailure(LocalStorageCallback.FailureEvent error) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        LocalStorage.get().get("locationSharing", new LocalStorageCallback() {

            @Override
            public void onSuccess(String value) {
                if (value != null) {
                    ObjectMapper om = new ObjectMapper();
                    try {
                        LocationSettings fromLs = om.readValue(value,
                                LocationSettings.class);
                        userName.setValue(fromLs.getUserName());
                        group.setValue(fromLs.getGroup());
                        locationSharing.setValue(fromLs.isLocationSharing());
                        deviceMappings.setValue(fromLs.getDeviceMappings());
                        trackingInterval.setValue(fromLs.getTrackingInterval());
                        if (trackingInterval.getValue() != null) {
                            amountOfRepeatedPositionRequests = 0;
                            Geolocator.detect(positionCallBack);
                        }
                        if (fromLs.isLocationSharing()) {
                            locate.click();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MobileUI.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void onFailure(LocalStorageCallback.FailureEvent error) {
                Notification.show(
                        "Local storage not allowed by your device!? " + error.
                        getMessage());
            }
        });

        locate.setDisableOnClick(true);
        mapView.setLeftComponent(locate);
        locate.addClickListener(e -> {
            amountOfRepeatedPositionRequests = 0;
            Geolocator.detect(positionCallBack);
            centerNextLocation = true;
        });
    }

    private void initMap() {
        map.removeAllComponents();
        final LTileLayer peruskartta = new LTileLayer(
                "http://v4.tahvonen.fi/mvm75/tiles/peruskartta/{z}/{x}/{y}.png");
//        final LTileLayer peruskartta = new LTileLayer(
//                "http://localhost:8888/tiles/peruskartta/{z}/{x}/{y}.png");
        peruskartta.setAttributionString("Maastokartta,Maanmittauslaitos");
        peruskartta.setDetectRetina(true);
        map.addLayer(peruskartta);
        zoomToContent();
    }

    private void showPopup(SpatialFeature f) {
        Notification.show("Notes for " + f.getTitle() + ":" + f.
                getDescription(), Notification.Type.WARNING_MESSAGE);
    }

    private void initSettingsView() {
        VerticalComponentGroup vcg = new VerticalComponentGroup();
        vcg.setCaption("Add layer");
        TextField textField = new MTextField("Name").withFullWidth();
        PasswordField pw = new PasswordField("Password");
        pw.setWidth("100%");
        Button button = new Button("Add");
        button.setWidth("100%");
        vcg.addComponents(textField, pw, button);
        button.addClickListener(e -> {
            UserGroup group = repo.findGroupByName(textField.
                    getValue());
            if (group.getReadOnlyPassword().equals(pw.getValue())) {
                LocalStorage.get().get("layers", new LocalStorageCallback() {

                    @Override
                    public void onSuccess(String value) {
                        if (value == null || value.isEmpty()) {
                            value = group.getId() + ",";
                        } else {
                            value += group.getId() + ",";
                        }
                        LocalStorage.get().
                                put("layers", value,
                                        new LocalStorageCallback() {

                                    @Override
                                    public void onSuccess(String value) {
                                        addGroup(group);
                                        if (navman.getCurrentComponent() == settingsView) {
                                            navman.navigateBack();
                                        }
                                    }

                                    @Override

                                    public void onFailure(
                                            LocalStorageCallback.FailureEvent error) {
                                        throw new UnsupportedOperationException(
                                                "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                    }

                                });
                    }

                    @Override
                    public void onFailure(
                            LocalStorageCallback.FailureEvent error
                    ) {
                        throw new UnsupportedOperationException(
                                "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
                return;
            }
            Notification.show("Name or pw didn't match!",
                    Notification.Type.WARNING_MESSAGE);

        });

        Button clearAll = new Button("Clear all layers");
        clearAll.addClickListener(e -> {
            LocalStorage.get().put("layers", "", new LocalStorageCallback() {

                @Override
                public void onSuccess(String value) {
                    Notification.show("Old layers cleared");
                    grouptToLayers.clear();
                    initMap();
                }

                @Override
                public void onFailure(LocalStorageCallback.FailureEvent error) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        });

        locationSharing.setEnabled(false);
        FieldEvents.TextChangeListener locationCanBeEnabled = e -> {
            TextField other;
            if (e.getComponent() == userName) {
                other = group;
            } else {
                other = userName;
            }
            locationSharing.setEnabled(StringUtils.isNotEmpty(other.
                    getValue()) && StringUtils.isNotEmpty(e.getText()));
            if (locationSharing.getValue() && !locationSharing.isEnabled()) {
                locationSharing.setValue(false);
            }
        };
        userName.addTextChangeListener(locationCanBeEnabled);
        group.addTextChangeListener(locationCanBeEnabled);
        MBeanFieldGroup.
                bindFieldsUnbuffered(service.getLocationSettings(), this);
        Button saveLocationSharingSettings = new Button(
                "Save location sharing settings");
        saveLocationSharingSettings.addClickListener(e -> {
            ObjectMapper om = new ObjectMapper();
            final LocationSettings locationSettings = service.
                    getLocationSettings();
            for (DeviceMapping dm : locationSettings.getDeviceMappings()) {
                locationRepository.saveDeviceMapping(dm);
            }
            try {
                String json = om.writeValueAsString(locationSettings);
                LocalStorage.get().put("locationSharing", json);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(MobileUI.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        });
        VerticalComponentGroup locationSharingLayout = new VerticalComponentGroup(
                "Location sharing");
        locationSharingLayout.addComponents(new RichText().withMarkDownResource(
                "/locationsharing.md"), userName, group, locationSharing,
                trackingInterval
        );

        settingsView.setContent(new CssLayout(vcg, clearAll,
                locationSharingLayout, new MVerticalLayout(deviceMappings),
                saveLocationSharingSettings));
    }

    protected void addGroup(UserGroup group) {
        if (group == null) {
            return;
        }
        if (grouptToLayers.containsKey(group)) {
            Notification.show("Layer already rendered");
            return;
        }
        LLayerGroup g = new LLayerGroup();
        List<SpatialFeature> feature = group.getFeatures();
        for (SpatialFeature f : feature) {
            LeafletLayer layer = JTSUtil.toLayer(f.getGeom());
            g.addComponent(layer);
            Style style = f.getStyle();
            if (layer instanceof AbstractLeafletVector) {
                AbstractLeafletVector v = (AbstractLeafletVector) layer;
                if (style != null) {
                    v.addClickListener(e -> showPopup(f));
                    v.setColor(style.getColor());
                } else {
                    v.setColor("green");
                }
            }
        }
        grouptToLayers.put(group, g);
        map.addLayer(g);
        zoomToContent();

    }

    protected void zoomToContent() {
        if (map.getComponentCount() == 1) {
            map.setCenter(60.4568759, 22.6870261);
            map.setZoomLevel(12);
        } else {
            map.zoomToContent();
        }
    }

}
