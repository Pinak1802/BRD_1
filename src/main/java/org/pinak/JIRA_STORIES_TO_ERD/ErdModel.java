package org.pinak.JIRA_STORIES_TO_ERD;



import java.util.List;

public class ErdModel {

    private List<ErdEntity> entities;
    private List<ErdRelationship> relationships;

    public ErdModel() {
    }

    public ErdModel(List<ErdEntity> entities,
                    List<ErdRelationship> relationships) {
        this.entities = entities;
        this.relationships = relationships;
    }

    public List<ErdEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<ErdEntity> entities) {
        this.entities = entities;
    }

    public List<ErdRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<ErdRelationship> relationships) {
        this.relationships = relationships;
    }

    @Override
    public String toString() {
        return "ErdModel{" +
                "entities=" + entities +
                ", relationships=" + relationships +
                '}';
    }
}
