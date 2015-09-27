package org.peimari.maastokanta.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.touchkit.extensions.Geolocator;
import com.vaadin.addon.touchkit.extensions.LocalStorage;
import com.vaadin.addon.touchkit.extensions.LocalStorageCallback;
import com.vaadin.addon.touchkit.extensions.PositionCallback;
import com.vaadin.addon.touchkit.gwt.client.vcom.Position;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.LocationRepository;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.Location;
import org.peimari.maastokanta.domain.LocationSettings;
import org.peimari.maastokanta.domain.LocationWithTail;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.Style;
import org.peimari.maastokanta.domain.UserGroup;
import org.peimari.maastokanta.tk102support.Update;
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

/**
 *
 * @author Matti Tahvonen <matti@vaadin.com>
 */
@SpringUI(path = "/")
@EnableAutoConfiguration
@Widgetset("org.peimari.maastokanta.MobileAppWidgetSet")
@Theme("touchkit")
@Title("Collamap")
public class MobileUI extends UI {

    private static final ScheduledExecutorService executor = Executors.
            newScheduledThreadPool(3);

    @Autowired
    AppService service;

    @Autowired
    Repository repo;

    @Autowired
    LocationRepository locationRepository;

    Map<UserGroup, LLayerGroup> grouptToLayers = new HashMap<>();

    NavigationView mapView = new NavigationView("Collamap");

    @Autowired
    SettingsView settingsView;

    NavigationManager navman = new NavigationManager(mapView);
    LMap map = new LMap();

    Button locate = new Button(FontAwesome.BULLSEYE);

    LLayerGroup others = new LFeatureGroup();

    LCircle me;

    private boolean centerNextLocation = false;
    private boolean viewPortInitialized = false;

    void removeAllLayers() {
        Notification.show("Old layers cleared");
        grouptToLayers.clear();
        initMap();
    }

    int amountOfRepeatedPositionRequests = 0;
    private static final int MAX_GEOLOC_CHECKS = 30;

    boolean badAccuracyReported = false;

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
            if (!badAccuracyReported && accuracy > 50) {
                Notification.show(
                        "Accurasy is pretty weak, only " + accuracy + " meters. Relocating again in couple of seconds might help.",
                        Notification.Type.TRAY_NOTIFICATION);
                badAccuracyReported = true;
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
            if (centerNextLocation) {
                double zoomlevel = findAppropriateZoomlevel(accuracy);
                if (service.getLocationSettings().getLastZoomLevel() == null || service.
                        getLocationSettings().getLastZoomLevel() < 13) {
                    map.setCenter(myloc, zoomlevel);
                } else {
                    map.setCenter(myloc, service.getLocationSettings().
                            getLastZoomLevel().doubleValue());
                }
                centerNextLocation = false;
                service.getLocationSettings().setLastCenter(myloc);
                int currentZoom = map.getZoomLevel().intValue();
                if (currentZoom < 13) {
                    currentZoom = (int) zoomlevel;
                }
                service.getLocationSettings().setLastZoomLevel(currentZoom);
                // TODO save settings to local storage every once in a while...
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
                drawOthers(groupName, myName);
                if (others.getParent() == null) {
                    map.addComponent(others);
                }
            }
            continueTracking();
        }

        public void drawOthers(final String groupName, final String myName) {
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
                    String msg = l.getName() + ", " + minutesAgo + "' ago, " + l.
                            getAccuracy() + "m accur.";

                    if (l instanceof LocationWithTail) {
                        LocationWithTail locationWithTail = (LocationWithTail) l;
                        final Update u = locationWithTail.
                                getLastUpdate();
                        msg = msg + " bat:" + u.getBatteryLevel()
                                + " GSM: " + u.getSignalLevel()
                                + " Sat: " + u.getFixCount()
                                + " Speed: " + u.getSpeed()
                                + " Course: " + u.getCourse()
                                ;

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

                    lMarker.setPopup(msg);
                    others.addComponent(lMarker);

                }
            }
        }

        public void continueTracking() {
            if (service.getLocationSettings().getTrackingInterval() != null) {
                final int intervalInMillis = service.getLocationSettings().
                        getTrackingInterval() * 1000;
                setPollInterval(intervalInMillis);
                executor.schedule(() -> {
                    access(() -> {
                        Geolocator.detect(positionCallBack);
                    });
                }, intervalInMillis, TimeUnit.MILLISECONDS);
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

        if (!viewPortInitialized && service.getLocationSettings().
                getLastCenter() != null && service.getLocationSettings().
                getLastZoomLevel() != null) {
            map.setCenter(service.getLocationSettings().getLastCenter(),
                    service.getLocationSettings().
                    getLastZoomLevel().doubleValue());
            viewPortInitialized = true;
        }

        LocalStorage.get().get("locationSharing", new LocalStorageCallback() {

            @Override
            public void onSuccess(String value) {
                if (value != null) {
                    ObjectMapper om = new ObjectMapper();
                    try {
                        LocationSettings settings = om.readValue(value,
                                LocationSettings.class);
                        service.setLocationSettings(settings);
                        settingsView.bindData();
                        if (settings.getTrackingInterval() != null) {
                            centerNextLocation = false;
                            amountOfRepeatedPositionRequests = 0;
                            Geolocator.detect(positionCallBack);
                        }
                        if (settings.isLocationSharing()) {
                            locate();
                        }
                        if (service.getLocationSettings().getLayers().isEmpty()) {
                            loadLayersLegacy();
                        } else {
                            loadLayers(service.getLocationSettings().getLayers());
                        }
                        
                        locationRepository.saveDeviceMappings(settings.getDeviceMappings());

                        if (!viewPortInitialized && settings.getLastCenter() != null && settings.
                                getLastZoomLevel() != null) {
                            map.setCenter(settings.getLastCenter(), settings.
                                    getLastZoomLevel().doubleValue());
                            viewPortInitialized = true;
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
            centerNextLocation = true;
            locate();
        });

        addPollListener(e -> {
            locationSavingStrategy.save();
        });
    }

    private void locate() {
        amountOfRepeatedPositionRequests = 0;
        Geolocator.detect(positionCallBack);
    }

    LocationSavingStrategy locationSavingStrategy = new LocationSavingStrategy();

    public class LocationSavingStrategy {

        private Instant lastUpdate;

        public void save() {
            if (navman.getCurrentComponent() == mapView) {
                service.getLocationSettings().setLastCenter(map.getCenter());
                service.getLocationSettings().setLastZoomLevel(map.
                        getZoomLevel().intValue());
                if (lastUpdate == null || lastUpdate.isBefore(Instant.now().
                        minusSeconds(30))) {
                    settingsView.saveSettings();
                    lastUpdate = Instant.now();
                }
            }
        }
    }

    public void loadLayersLegacy() {
        LocalStorage.get().get("layers", new LocalStorageCallback() {

            @Override
            public void onSuccess(String value) {
                if (value != null) {
                    Logger.getLogger(MobileUI.class.getName()).
                            log(Level.SEVERE, "Loaded legacy layers");
                    List<String> groupIds = Arrays.asList(value.split(","));
                    loadLayers(groupIds);
                    service.getLocationSettings().setLayers(groupIds);
                    settingsView.saveSettings();
                }
                if (!viewPortInitialized) {
                    zoomToContent();
                    viewPortInitialized = true;
                }
            }

            @Override
            public void onFailure(LocalStorageCallback.FailureEvent error) {
            }
        });
    }

    public void loadLayers(List<String> groupIds) {
        for (String id : groupIds) {
            addGroup(repo.getGroup(id));
        }
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
