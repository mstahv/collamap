package org.peimari.maastokanta.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.addon.leaflet.shared.Point;

/**
 *
 * @author matti ät vaadin.com
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationSettings implements Serializable {

    private String group;

    private String userName;

    private boolean locationSharing;

    private Integer trackingInterval;

    private List<DeviceMapping> deviceMappings = new ArrayList<>();
    private List<String> layers = new ArrayList();

    private Double lastCenterLon;
    private Double lastCenterLat;

    private Integer lastZoomLevel;

    /**
     * Get the value of locationSharing
     *
     * @return the value of locationSharing
     */
    public boolean isLocationSharing() {
        return locationSharing;
    }

    /**
     * Set the value of locationSharing
     *
     * @param locationSharing new value of locationSharing
     */
    public void setLocationSharing(boolean locationSharing) {
        this.locationSharing = locationSharing;
    }

    /**
     * Get the value of userName
     *
     * @return the value of userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the value of userName
     *
     * @param userName new value of userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the value of group
     *
     * @return the value of group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the value of group
     *
     * @param group new value of group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    public List<DeviceMapping> getDeviceMappings() {
        return deviceMappings;
    }

    public void setDeviceMappings(List<DeviceMapping> deviceMappings) {
        this.deviceMappings = deviceMappings;
    }

    public Integer getTrackingInterval() {
        return trackingInterval;
    }

    public void setTrackingInterval(Integer trackingInterval) {
        this.trackingInterval = trackingInterval;
    }

    @JsonIgnore
    public Point getLastCenter() {
        if (lastCenterLat == null) {
            return null;
        }
        return new Point(lastCenterLat, lastCenterLon);
    }

    public void setLastCenter(Point lastCenter) {
        if (lastCenter == null) {
            lastCenterLat = null;
            lastCenterLon = null;
        } else {
            lastCenterLat = lastCenter.getLat();
            lastCenterLon = lastCenter.getLon();
        }
    }

    public Integer getLastZoomLevel() {
        return lastZoomLevel;
    }

    public void setLastZoomLevel(Integer lastZoomLevel) {
        this.lastZoomLevel = lastZoomLevel;
    }

    public List<String> getLayers() {
        return layers;
    }

    public void setLayers(List<String> layers) {
        this.layers = layers;
    }

    public Double getLastCenterLon() {
        return lastCenterLon;
    }

    public void setLastCenterLon(Double lastCenterLon) {
        this.lastCenterLon = lastCenterLon;
    }

    public Double getLastCenterLat() {
        return lastCenterLat;
    }

    public void setLastCenterLat(Double lastCenterLat) {
        this.lastCenterLat = lastCenterLat;
    }

}
