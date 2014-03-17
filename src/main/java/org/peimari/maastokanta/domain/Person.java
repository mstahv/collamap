/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mstahv
 */
public class Person implements Serializable {
    
    static final long serialVersionUID = 1L;

    private String displayName;

    private String email;
    
    private final Map<String,String> idToGroup = new HashMap<>();

    public Person() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void addGroup(String id, String name) {
        idToGroup.put(id, name);
    }

    public Map<String, String> getIdToGroup() {
        return idToGroup;
    }

}
