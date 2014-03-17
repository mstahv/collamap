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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.SerializationUtils;
import org.peimari.maastokanta.domain.Person;
import org.peimari.maastokanta.domain.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class Repository {

    File root;
    File persons;

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
                emailToPerson = (HashMap<String, Person>) deserializeFromFile("_persons");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Object deserializeFromFile(String fileName) throws FileNotFoundException {
        return SerializationUtils.deserialize(new FileInputStream(new File(root, fileName)));
    }

    public void persist(UserGroup userGroup) {
        // TODO implement optimistic locking
        try {
            SerializationUtils.serialize(userGroup, new FileOutputStream(new File(root, userGroup.getId())));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void saveUsers() {
        try {
            SerializationUtils.serialize(emailToPerson, new FileOutputStream(persons));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
