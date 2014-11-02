package org.peimari.maastokanta.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserGroup extends AbstractEntity {

    static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();

    private String name;

    private String readOnlyPassword;

    private List<String> editorEmails = new ArrayList<>();

    public String getReadOnlyPassword() {
        return readOnlyPassword;
    }

    public void setReadOnlyPassword(String readOnlyPassword) {
        this.readOnlyPassword = readOnlyPassword;
    }

    private List<SpatialFeature> features = new ArrayList<>();

    private List<Style> styles = new ArrayList<>();

    public UserGroup() {
    }

    public String getId() {
        return id;
    }

    public UserGroup(String name) {
        this.name = name;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public String getName() {
        return name;
    }

    public List<SpatialFeature> getFeatures() {
        return features;
    }

    public List<SpatialFeature> getFeatures(String filter) {
        return getFeatures().stream().filter(p -> p.toString().toLowerCase()
                .contains(filter.toLowerCase())).collect(Collectors.toList());
    }

    public void setFeatures(List<SpatialFeature> features) {
        this.features = features;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Style[name:" + name + "]";
    }

    public void addStyle(String name, String color) {
        styles.add(new Style(name, color));
    }

    public List<String> getEditorEmails() {
        if (editorEmails == null) {
            editorEmails = new ArrayList<>();
        }
        return editorEmails;
    }

    public void setEditorEmails(List<String> editorEmails) {
        this.editorEmails = editorEmails;
    }

}
