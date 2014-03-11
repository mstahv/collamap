/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author mstahv
 */
@Entity
public class Person {

    private String displayName;

    @Id
    private String email;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Person) {
            Person person = (Person) obj;
            return email.equals(person.email);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
    
    
    
}
