package org.pinak.JIRA_STORIES_TO_ERD;


import java.util.List;

public class ErdEntity {

    private String name;
    private List<ErdAttribute> attributes;

    public ErdEntity() {
    }

    public ErdEntity(String name, List<ErdAttribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ErdAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ErdAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "ErdEntity{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
