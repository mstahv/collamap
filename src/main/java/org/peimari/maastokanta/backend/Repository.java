/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.peimari.maastokanta.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.SerializationUtils;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Repository {

    File root;
    File persons;
    File nameToIdFile;

    HashMap<String, String> nameToId = new HashMap<>();

    HashMap<String, Person> emailToPerson = new HashMap<>();

    HashMap<String, WeakReference<UserGroup>> idToGroup = new HashMap<>();

    @Autowired
    Environment env;

    @PostConstruct
    void setup() {
        root = new File(env.getProperty("fileroot", "/tmp/collamap"));
        root.mkdirs();
        persons = new File(root, "_persons");
        if (persons.exists()) {
            try {
                emailToPerson = (HashMap<String, Person>) deserializeFromFile(
                        "_persons");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Repository.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        nameToIdFile = new File(root, "_names");
        if (nameToIdFile.exists()) {
            try {
                nameToId = (HashMap<String, String>) deserializeFromFile(
                        "_names");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Repository.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
    }

    public synchronized Person getPerson(String email) {
        return emailToPerson.get(email);
    }

    public synchronized void persist(Person p) {
        emailToPerson.put(p.getEmail(), p);
        saveUsers();
    }

    public UserGroup getGroup(String id) {
        synchronized (idToGroup) {
            WeakReference<UserGroup> ref = idToGroup.get(id);
            if (ref != null && ref.get() != null) {
                return ref.get();
            }
        }
        try {
            UserGroup userGroup = (UserGroup) deserializeFromFile(id);
            synchronized (idToGroup) {
                idToGroup.put(id, new WeakReference<>(userGroup));
            }
            return userGroup;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null,
                    ex);
            return null;
        }
    }

    private Object deserializeFromFile(String fileName) throws FileNotFoundException {
        return SerializationUtils.deserialize(new FileInputStream(new File(root,
                fileName)));
    }

    public synchronized void updateName(UserGroup userGroup, String newName) {
        nameToId.remove(userGroup.getName());
        userGroup.setName(newName);
        nameToId.put(userGroup.getName(), userGroup.getId());
        saveNames();
    }

    public synchronized void persist(UserGroup userGroup) {
        // TODO implement optimistic locking
        try {
            SerializationUtils.serialize(userGroup, new FileOutputStream(
                    new File(root, userGroup.getId())));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    public synchronized void saveUsers() {
        try {
            SerializationUtils.serialize(emailToPerson, new FileOutputStream(
                    persons));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    public synchronized void saveNames() {
        try {
            SerializationUtils.serialize(nameToId, new FileOutputStream(
                    nameToIdFile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    public UserGroup findGroupByName(String value) {
        String id = nameToId.get(value);
        return getGroup(id);
    }

    public void updateEditors(UserGroup group, List<String> oldEditors) {
        List<String> orphaned = oldEditors;
        for (String email : group.getEditorEmails()) {
            Person person = getPerson(email);
            if (person == null) {
                person = new Person();
                person.setEmail(email);
                person.setDisplayName(email.substring(0, email.indexOf("@")));
                persist(person);
            }
            person.getIdToGroup().put(group.getId(), group.getName());
            orphaned.remove(email);
        }
        for (String email : orphaned) {
            getPerson(email).getIdToGroup().remove(group.getId());
        }
        saveUsers();
    }
}
