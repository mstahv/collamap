package org.peimari.maastokanta.domain;

import org.springframework.data.neo4j.annotation.GraphId;

public class AbstractEntity {

    @GraphId
    private Long id;

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj.getClass() == this.getClass()) && id != null) {
            return id.equals(((AbstractEntity) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

}
