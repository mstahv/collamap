package org.peimari.maastokanta.domain;

public class Style extends AbstractEntity {
    
    static final long serialVersionUID = 1L;

    private String name;
    private String color;

    public Style() {
    }

    Style(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Style[name:" + name + "]";
    }

}
