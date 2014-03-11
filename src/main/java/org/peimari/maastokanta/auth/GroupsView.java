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
import org.peimari.maastokanta.backend.UserService;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.fields.MTable;
import org.vaadin.maddon.fields.MTextField;
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
    UserService userService;
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
                UserGroup group = new UserGroup();
                group.setName(groupName);
                group.setAdmin(userService.getPerson());
                group = groupRepository.save(group);
                userService.setGroup(group);
                Page.getCurrent().setLocation("/");
            }
        }
    });

    void init() {
        MTable existing = new MTable().withColumnHeaders("Name");
        existing.setHeight("150px");

        MTable available = new MTable().withColumnHeaders("Name");
        available.setHeight("150px");

        addComponents(header, new MVerticalLayout(existing), new MVerticalLayout(joinHeader, available, newGroupheader), new MVerticalLayout(newName, createNew));
    }

}
