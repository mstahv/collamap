/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.Style;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "session")
public class AppService {
    
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    PersonRepository personRepository;

    private Person person;
    private UserGroup group;
    private List<Style> styles = new ArrayList<>();
    private ArrayList<UserGroup> groups;

    public boolean isAuthtenticated() {
        return group != null;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        groups = new ArrayList<>(person.getGroups());
        this.person = person;
    }

    public ArrayList<UserGroup> getGroups() {
        return groups;
    }

    public void setGroup(UserGroup g) {
        this.group = g;
    }

    public UserGroup getGroup() {
        return group;
    }
    
    public List<Style> getStyles() {
        return Collections.unmodifiableList(styles);
    }
    
    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public UserGroup createNewGroup(String groupName) {
        UserGroup group = new UserGroup();
        group.setName(groupName);
        group.setAdmin(getPerson());
        group.addStyle("Normal", "blue");
        group.addStyle("Important", "red");
        group = groupRepository.save(group);
        getPerson().getGroups().add(group);
        person = personRepository.save(getPerson());
        setStyles(group.getStyles());
        return group;
    }

}
