package org.pinak.ERD_SYSTEM_DESIGN;


public class SystemDesignMermaidGenerator {

    public static String generate(
            SystemDesignModel model) {

        StringBuilder mermaid = new StringBuilder();

        mermaid.append("flowchart LR\n");

        // ========================================
        // COMPONENTS
        // ========================================

        for (SystemDesignModel.Component component
                : model.getComponents()) {

            String id = sanitize(component.getId());

            String label =
                    component.getName()
                            + "<br/>"
                            + component.getTechnology();

            mermaid.append("    ")
                    .append(id)
                    .append("[\"")
                    .append(label)
                    .append("\"]")
                    .append("\n");
        }

        mermaid.append("\n");

        // ========================================
        // CONNECTIONS
        // ========================================

        for (SystemDesignModel.Connection connection
                : model.getConnections()) {

            String from = sanitize(connection.getFrom());
            String to = sanitize(connection.getTo());

            String label = connection.getProtocol();

            mermaid.append("    ")
                    .append(from)
                    .append(" -->|")
                    .append(label)
                    .append("| ")
                    .append(to)
                    .append("\n");
        }

        return mermaid.toString();
    }

    private static String sanitize(String value) {

        if (value == null) {
            return "unknown";
        }

        return value
                .replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
