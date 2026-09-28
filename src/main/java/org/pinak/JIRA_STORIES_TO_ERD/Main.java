package org.pinak.JIRA_STORIES_TO_ERD;

import tools.jackson.databind.ObjectMapper;

import static org.pinak.JIRA_STORIES_TO_ERD.ErdGenerator.callGemini;
import static org.pinak.JIRA_STORIES_TO_ERD.ErdGenerator.getJiraStories;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("========================================");
        System.out.println("DOWNLOADING JIRA USER STORIES");
        System.out.println("========================================");

        String stories = getJiraStories();

        System.out.println();
        System.out.println("========================================");
        System.out.println("MERGED JIRA STORIES");
        System.out.println("========================================");

        System.out.println(stories);

        // ONE LLM CALL FOR ALL STORIES
        String erdJson = callGemini(stories);

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED ERD JSON");
        System.out.println("========================================");

        System.out.println(erdJson);

        // ========================================
        // CONVERT JSON TO JAVA OBJECT
        // ========================================

        ObjectMapper mapper = new ObjectMapper();

        ErdModel erdModel =
                mapper.readValue(erdJson, ErdModel.class);

        System.out.println();
        System.out.println("========================================");
        System.out.println("ERD JAVA OBJECT CREATED");
        System.out.println("========================================");

        System.out.println("Number of entities: "
                + erdModel.getEntities().size());

        System.out.println("Number of relationships: "
                + erdModel.getRelationships().size());

        // ========================================
        // GENERATE MERMAID ERD
        // ========================================

        String mermaid =
                ErdMermaidGenerator.generate(erdModel);

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED MERMAID ERD");
        System.out.println("========================================");

        System.out.println(mermaid);

        // Generate actual ERD image
        ErdDiagramGenerator.generateDiagram(mermaid);
    }
}