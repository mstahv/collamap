package org.peimari.maastokanta.mobile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.touchkit.extensions.LocalStorage;
import com.vaadin.addon.touchkit.extensions.LocalStorageCallback;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.event.FieldEvents;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.LocationRepository;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.DeviceMapping;
import org.peimari.maastokanta.domain.LocationSettings;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.MBeanFieldGroup;
import org.vaadin.viritin.fields.ElementCollectionField;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.label.RichText;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author Matti Tahvonen
 */
@SpringComponent
@UIScope
public class SettingsView extends NavigationView {

    @Autowired
    AppService service;

    @Autowired
    Repository repo;

    @Autowired
    LocationRepository locationRepository;

    private NavigationButton.NavigationButtonClickListener saveOnBack;

    MTextField userName = new MTextField("Username");
    MTextField group = new MTextField("Sharing group");
    private VerticalComponentGroup addNewLayerGroup;

    public static class DeviceMappingFields {

        public MTextField imei = new MTextField().withFullWidth();
        public MTextField name = new MTextField().withWidth("40px");
        public MTextField group = new MTextField().withWidth("40px");
        public Switch enabled = new Switch("");

    }

    ElementCollectionField<DeviceMapping> deviceMappings = new ElementCollectionField<>(
            DeviceMapping.class, DeviceMappingFields.class).
            withCaption("Devices").expand("imei").withFullWidth().
            setAllowRemovingItems(false);

    Switch locationSharing = new Switch("Share location");

    TypedSelect<Integer> trackingInterval = new TypedSelect(
            "Tracking interval(s)").setOptions(5, 10, 30).withFullWidth();

    public SettingsView() {
        setCaption("Settings");
    }

    @Override
    protected void onBecomingVisible() {
        super.onBecomingVisible();
        if (saveOnBack == null) {
            saveOnBack = e -> {
                saveSettings();
            };
            ((NavigationButton) getLeftComponent()).addClickListener(saveOnBack);
            initSettingsView();
        }
    }

    public void saveSettings() {
        ObjectMapper om = new ObjectMapper();
        final LocationSettings locationSettings = service.
                getLocationSettings();
        Collection value = deviceMappings.getValue();
        locationSettings.setDeviceMappings((List<DeviceMapping>) value);

        locationRepository.saveDeviceMappings(locationSettings.getDeviceMappings());
        try {
            String json = om.writeValueAsString(locationSettings);
            LocalStorage.get().put("locationSharing", json);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MobileUI.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    private void initSettingsView() {
        addNewLayerGroup = new VerticalComponentGroup();
        TextField textField = new MTextField("Name").withFullWidth();
        PasswordField pw = new PasswordField("Password");
        pw.setWidth("100%");
        Button addNewLayer = new Button("Add");
        addNewLayer.setWidth("100%");
        addNewLayerGroup.addComponents(textField, pw, addNewLayer);
        addNewLayer.addClickListener(e -> {
            UserGroup newGroup = repo.findGroupByName(textField.
                    getValue());
            if (newGroup.getReadOnlyPassword().equals(pw.getValue())) {
                final List<String> layers = service.getLocationSettings().
                        getLayers();
                if (layers.contains(newGroup.getId())) {
                    Notification.
                            show("You already have layer" + newGroup.getName());
                    return;
                }
                layers.add(newGroup.getId());
                saveSettings();

                MobileUI ui = (MobileUI) getNavigationManager().
                        getPreviousComponent();
                ui.addGroup(newGroup);
                if (getNavigationManager().
                        getCurrentComponent() == SettingsView.this) {
                    getNavigationManager().
                            navigateBack();
                }
                ui.zoomToContent();
            } else {
                Notification.show("Name or pw didn't match!",
                        Notification.Type.WARNING_MESSAGE);

            }

        });

        updateLayersCount();

        Button clearAll = new Button("Clear all layers");
        clearAll.addClickListener(e -> {
            LocalStorage.get().put("layers", "", new LocalStorageCallback() {

                @Override
                public void onSuccess(String value) {
                    MobileUI ui = (MobileUI) getNavigationManager().
                            getPreviousComponent();
                    ui.removeAllLayers();
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

        VerticalComponentGroup locationSharingLayout = new VerticalComponentGroup(
                "Location sharing");
        locationSharingLayout.addComponents(new RichText().withMarkDownResource(
                "/locationsharing.md"), userName, group, locationSharing,
                trackingInterval
        );

        VerticalComponentGroup devHelp = new VerticalComponentGroup();
        NavigationButton navigationButton = new NavigationButton("Device help");
        navigationButton.addClickListener(e -> {
            getNavigationManager().navigateTo(new HelpView("/devices.md"));
        });
        devHelp.addComponent(navigationButton);

        setContent(new CssLayout(addNewLayerGroup, clearAll,
                locationSharingLayout, new MVerticalLayout(devHelp,
                        deviceMappings).withCaption("Devices")));
    }

    public void updateLayersCount() {
        addNewLayerGroup.setCaption("Add new layer (currently " + service.
                getLocationSettings().getLayers().size()
                + " layer(s))");
    }

    void bindData() {
        // TODO use proper databinding with MBeanFieldGroup
        LocationSettings fromLs = service.getLocationSettings();
        userName.setValue(fromLs.getUserName());
        group.setValue(fromLs.getGroup());
        locationSharing.setValue(fromLs.isLocationSharing());
        deviceMappings.setValue(fromLs.getDeviceMappings());
        trackingInterval.setValue(fromLs.getTrackingInterval());
    }

}
