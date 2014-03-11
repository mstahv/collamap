/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.peimari.maastokanta.backend.PersonRepository;
import org.peimari.maastokanta.backend.UserService;
import org.peimari.maastokanta.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.maddon.label.Header;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.VaadinUI;

/**
 *
 * @author mstahv
 */
@VaadinUI(path = "/auth")
public class AuthenticationUI extends UI {

    @Autowired
    UserService userService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    GroupsView groupsView;

    @Autowired
    LoginView loginView;

    @Override
    protected void init(VaadinRequest request) {
        setPollInterval(1000);
        setContent(loginView);
    }

    public void setUser(String email, String displayName) {
        Person person = personRepository.findOne(email);
        if (person == null) {
            person = new Person();
            person.setEmail(email);
            person.setDisplayName(displayName);
            person = personRepository.save(person);
        }
        userService.setPerson(person);
        groupsView.init();
        setContent(groupsView);
    }

    public static AuthenticationUI get() {
        return (AuthenticationUI) getCurrent();
    }

}
