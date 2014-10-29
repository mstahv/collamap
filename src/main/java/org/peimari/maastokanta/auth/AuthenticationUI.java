/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.VaadinUI;

/**
 *
 * @author mstahv
 */
@VaadinUI(path = "/auth")
@Theme("valo")
public class AuthenticationUI extends UI {

    @Autowired
    AppService service;

    @Autowired
    Repository repo;

    @Autowired
    GroupsView groupsView;

    @Autowired
    LoginView loginView;

    @Override
    protected void init(VaadinRequest request) {
        setPollInterval(1000);
        if(service.getPerson() != null) {
            setContent(groupsView);
        } else {
            setContent(loginView);
        }
    }

    public void setUser(String email, String displayName) {
        Person person = repo.getPerson(email);
        if (person == null) {
            person = new Person();
            person.setEmail(email);
            person.setDisplayName(displayName);
            repo.persist(person);
        }
        service.setPerson(person);
        setContent(groupsView);
    }

    public static AuthenticationUI get() {
        return (AuthenticationUI) getCurrent();
    }

}
