/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.auth;

import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import java.util.Map.Entry;
import org.peimari.maastokanta.backend.AppService;
import org.peimari.maastokanta.backend.Repository;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 *
 * @author mstahv
 */
@UIScope
@VaadinComponent
class GroupsView extends MVerticalLayout {

    @Autowired
    AppService service;
    @Autowired
    Repository groupRepository;

    Header header = new Header("Select group:");

    Header newGroupheader = new Header("Create new:").setHeaderLevel(3);

    Header joinHeader = new Header("Request to join existing:").
            setHeaderLevel(3);

    TextField newName = new MTextField();
    
    TextField forceOpenId = new MTextField();

    Button createNew = new MButton("Create", new Button.ClickListener() {

        @Override
        public void buttonClick(Button.ClickEvent event) {
            final String groupName = newName.getValue();
            if (groupName.isEmpty()) {
                Notification.show("Type in the name of the group");
            } else {
                UserGroup group = service.createNewGroup(groupName);
                userGroup(group);
            }
        }

    });

    protected void userGroup(UserGroup group) {
        service.setGroup(group);
        Page.getCurrent().setLocation(Page.getCurrent().
                getLocation().toString().replace("/auth", "/admin"));
    }

    @Override
    public void attach() {
        super.attach();
        Table existing = new Table();
        existing.setHeight("300px");
        existing.addContainerProperty("name", String.class, "");
        for (Entry<String, String> e : service.getPerson().getIdToGroup().
                entrySet()) {
            existing.addItem(e.getKey()).getItemProperty("name").setValue(e.
                    getValue());
        }
        existing.setSelectable(true);
        existing.setImmediate(true);
        existing.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object value = event.getProperty().getValue();
                if (value != null) {
                    UserGroup group = groupRepository.getGroup((String) event.
                            getProperty().getValue());
                    service.setGroup(group);
                    Page.getCurrent().setLocation(Page.getCurrent().
                            getLocation().toString().replace("/auth", "/admin"));
                }
            }
        });

//        MTable available = new MTable(UserGroup.class).
//                withProperties("name").withHeight("150px")
//                .setBeans(groupRepository.findAll());
//        available.addMValueChangeListener(new MValueChangeListener() {
//
//            @Override
//            public void valueChange(MValueChangeEvent event) {
//                Notification.show("TODO make join request");
//            }
//        });
        
        Button forceOpen = new Button("Force open with id");
        forceOpen.addClickListener(e->{
            UserGroup group = groupRepository.getGroup(forceOpenId.getValue());
            if(group != null) {
                userGroup(group);
            }
        });
      
        withSpacing(false);
        addComponents(header,
                new MVerticalLayout(new Header("Choose existing:").setHeaderLevel(3), existing),
                new MVerticalLayout(newGroupheader,newName, createNew),
                new MVerticalLayout(forceOpenId,forceOpen).withCaption("Force open with id (admin only)")
        );
    }

}
