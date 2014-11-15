package org.peimari.maastokanta;

import com.vaadin.addon.touchkit.extensions.Geolocator;
import com.vaadin.addon.touchkit.extensions.LocalStorage;
import com.vaadin.addon.touchkit.extensions.LocalStorageCallback;
import com.vaadin.addon.touchkit.extensions.PositionCallback;
import com.vaadin.addon.touchkit.gwt.client.vcom.Position;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.SpatialFeature;
import org.peimari.maastokanta.domain.Style;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.vaadin.addon.leaflet.AbstractLeafletVector;
import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletLayer;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.addon.leaflet.util.JTSUtil;
import org.vaadin.maddon.fields.MTextField;
import org.vaadin.spring.touchkit.TouchKitUI;

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

    Map<UserGroup, LLayerGroup> grouptToLayers = new HashMap<>();

    NavigationView mapView = new NavigationView("Collamap");
    NavigationView settingsView = new NavigationView("Settings");
    NavigationManager navman = new NavigationManager(mapView);
    LMap map = new LMap();

    LCircle me;

    @Override
    protected void init(VaadinRequest request) {
        initMap();
        initSettingsView();
        mapView.setSizeFull();
        mapView.setContent(map);
        final NavigationButton settingsButton = new NavigationButton();
        settingsButton.setIcon(FontAwesome.NAVICON);
        settingsButton.addClickListener(e->{navman.navigateTo(settingsView);});
        mapView.setRightComponent(settingsButton);
        navman.setNextComponent(settingsView);
        setContent(navman);

        LocalStorage.get().get("layers", new LocalStorageCallback() {

            @Override
            public void onSuccess(String value) {
                for (String id : value.split(",")) {
                    addGroup(repo.getGroup(id));
                }
            }

            @Override
            public void onFailure(LocalStorageCallback.FailureEvent error) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        Button locate = new Button(FontAwesome.BULLSEYE);
        locate.setDisableOnClick(true);
        mapView.setLeftComponent(locate);
        locate.addClickListener(e -> {
            Geolocator.detect(new PositionCallback() {

                @Override
                public void onSuccess(Position position) {
                    final Point myloc = new Point(position.getLatitude(),
                            position.getLongitude());
                    double accuracy = position.getAccuracy();
                    if (accuracy > 50) {
                        Notification.show(
                                "Accurasy is pretty weak, only " + accuracy + " meters. Relocating in couple of seconds might help.");
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
                    }
                    map.setCenter(myloc);
                    int zoomlevel = findAppropriateZoomlevel(accuracy);
                    map.setZoomLevel(zoomlevel);
                    locate.setEnabled(true);
                }

                @Override
                public void onFailure(int errorCode) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                private int findAppropriateZoomlevel(double accuracy) {
                    final int screenW = MobileUI.this.getPage().
                            getBrowserWindowWidth();
                    double[] mPerPixel = new double[]{156412, 78206, 39103, 19551, 9776, 4888, 2444, 1222, 610.984, 305.492, 152.746, 76.373, 38.187, 19.093, 9.547, 4.773, 2.387, 1.193, 0.596, 0.298};
                    int zoomLevel = 0;
                    for (int i = 0; i < mPerPixel.length; i++) {
                        double metersOnScreen = mPerPixel[i]*screenW;
                        if (metersOnScreen / 15.0 > accuracy) {
                            zoomLevel = i;
                        } else {
                            break;
                        }
                    }
                    if (zoomLevel > 16) {
                        zoomLevel = 16;
                    }
                    return zoomLevel;
                }
            });
        });
    }

    private void initMap() {
        map.removeAllComponents();
        final LTileLayer peruskartta = new LTileLayer(
                "http://v3.tahvonen.fi/mvm71/tiles/peruskartta/{z}/{x}/{y}.png");
        peruskartta.setDetectRetina(true);
        map.addLayer(peruskartta);
        zoomToContent();
    }

    private void showPopup(SpatialFeature f) {
        Notification.show("Notes for " + f.getTitle() + ":" + f.
                getDescription(),Notification.Type.WARNING_MESSAGE);
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

        settingsView.setContent(new CssLayout(vcg, clearAll, new Label(
                "TODO location sharing")));
    }

    protected void addGroup(UserGroup group) {
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
