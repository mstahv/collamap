/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import javax.annotation.PostConstruct;
import org.peimari.maastokanta.backend.GroupRepository;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.fields.MTable;
import org.vaadin.maddon.fields.MTextField;
import org.vaadin.maddon.fields.MValueChangeEvent;
import org.vaadin.maddon.fields.MValueChangeListener;
import org.vaadin.maddon.label.Header;
import org.vaadin.maddon.layouts.MVerticalLayout;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

/**
 *
 * @author mstahv
 */
@UIScope
@VaadinComponent
class GroupsView extends MVerticalLayout {

    @Autowired
    AppService userService;
    @Autowired
    GroupRepository groupRepository;

    Header header = new Header("Select group:");

    Header newGroupheader = new Header("Create new:").setHeaderLevel(3);

    Header joinHeader = new Header("Request to join existing:").setHeaderLevel(3);

    TextField newName = new MTextField();

    Button createNew = new MButton("Create", new Button.ClickListener() {

        @Override
        public void buttonClick(Button.ClickEvent event) {
            final String groupName = newName.getValue();
            if (groupName.isEmpty()) {
                Notification.show("Type in the name of the group");
            } else {
                UserGroup group = userService.createNewGroup(groupName);
                userGroup(group);
            }
        }

    });

    protected void userGroup(UserGroup group) {
        userService.setGroup(group);
        Page.getCurrent().setLocation("/");
    }

    @Override
    public void attach() {
        super.attach();
        MTable<UserGroup> existing = new MTable(UserGroup.class).
                withProperties("name").addBeans(userService.getGroups())
                .withHeight("150px");
        existing.addMValueChangeListener(new MValueChangeListener<UserGroup>() {

            @Override
            public void valueChange(MValueChangeEvent<UserGroup> event) {
                userGroup(event.getValue());
            }
        });

        MTable available = new MTable(UserGroup.class).
                withProperties("name").withHeight("150px")
                .setBeans(groupRepository.findAll());
        available.addMValueChangeListener(new MValueChangeListener() {

            @Override
            public void valueChange(MValueChangeEvent event) {
                Notification.show("TODO make join request");
            }
        });

        addComponents(header, new MVerticalLayout(existing),
                new MVerticalLayout(joinHeader, available, newGroupheader),
                new MVerticalLayout(newName, createNew));
    }

}
