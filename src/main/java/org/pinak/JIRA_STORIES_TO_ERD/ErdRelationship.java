package org.pinak.JIRA_STORIES_TO_ERD;



public class ErdRelationship {

    private String from;
    private String to;
    private String type;
    private String cardinality;

    public ErdRelationship() {
    }

    public ErdRelationship(String from, String to,
                           String type, String cardinality) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.cardinality = cardinality;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public String toString() {
        return "ErdRelationship{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", type='" + type + '\'' +
                ", cardinality='" + cardinality + '\'' +
                '}';
    }
}
