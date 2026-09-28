package org.pinak.JIRA_STORIES_TO_ERD;

public class ErdMermaidGenerator {

    public static String generate(ErdModel model) {

        StringBuilder mermaid = new StringBuilder();

        mermaid.append("erDiagram\n");

        // ==============================
        // ENTITIES
        // ==============================

        for (ErdEntity entity : model.getEntities()) {

            mermaid.append("    ")
                    .append(entity.getName())
                    .append(" {\n");

            for (ErdAttribute attribute : entity.getAttributes()) {

                mermaid.append("        ")
                        .append(attribute.getType())
                        .append(" ")
                        .append(attribute.getName());

                if (attribute.isPrimaryKey()) {
                    mermaid.append(" PK");
                } else if (attribute.isForeignKey()) {
                    mermaid.append(" FK");
                }

                mermaid.append("\n");
            }

            mermaid.append("    }\n");
        }

        // ==============================
        // RELATIONSHIPS
        // ==============================

        for (ErdRelationship relationship :
                model.getRelationships()) {

            String cardinality =
                    convertCardinality(
                            relationship.getCardinality()
                    );

            mermaid.append("    ")
                    .append(relationship.getFrom())
                    .append(" ")
                    .append(cardinality)
                    .append(" ")
                    .append(relationship.getTo())
                    .append(" : ")
                    .append("\"")
                    .append(relationship.getType())
                    .append("\"")
                    .append("\n");
        }

        return mermaid.toString();
    }

    private static String convertCardinality(String cardinality) {

        return switch (cardinality) {

            case "1:1" -> "||--||";

            case "1:N" -> "||--o{";

            case "N:1" -> "}o--||";

            case "M:N" -> "}o--o{";

            default -> "||--o{";
        };
    }
}
