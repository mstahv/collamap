package org.peimari.maastokanta.domain;

import java.io.Serializable;

/**
 *
 * @author matti ät vaadin.com
 */
public class LocationSettings implements Serializable {

    private String group;

    private String userName;

    private boolean locationSharing;

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

}
