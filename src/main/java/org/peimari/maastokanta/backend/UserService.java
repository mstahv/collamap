/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.peimari.maastokanta.backend;

import org.peimari.maastokanta.domain.UserGroup;
import org.peimari.maastokanta.domain.Person;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "session")
public class UserService {
    
    private Person person;
    private UserGroup group;
    
    public boolean isAuthtenticated() {
        return group != null;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
    
    public void setGroup(UserGroup g) {
        this.group = g;
    }

    public UserGroup getGroup() {
        return group;
    }
    
}
