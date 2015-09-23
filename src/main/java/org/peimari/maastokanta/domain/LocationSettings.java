package org.peimari.maastokanta.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author matti ät vaadin.com
 */
public class LocationSettings implements Serializable {

    private String group;

    private String userName;

    private boolean locationSharing;

    private Integer trackingInterval;

    private List<DeviceMapping> deviceMappings = new ArrayList<>();

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

}
