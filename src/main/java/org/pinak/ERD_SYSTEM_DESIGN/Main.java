package org.pinak.ERD_SYSTEM_DESIGN;

import org.pinak.JIRA_STORIES_TO_ERD.ErdDiagramGenerator;
import org.pinak.JIRA_STORIES_TO_ERD.ErdMermaidGenerator;
import org.pinak.JIRA_STORIES_TO_ERD.ErdModel;
import tools.jackson.databind.ObjectMapper;

import static org.pinak.JIRA_STORIES_TO_ERD.ErdGenerator.callGemini;
import static org.pinak.JIRA_STORIES_TO_ERD.ErdGenerator.getJiraStories;

public class Main {

    public static void main(String[] args) throws Exception {

        // ========================================
        // STEP 1
        // DOWNLOAD JIRA STORIES
        // ========================================

        System.out.println("========================================");
        System.out.println("DOWNLOADING JIRA USER STORIES");
        System.out.println("========================================");

        String stories = getJiraStories();

        System.out.println();
        System.out.println("========================================");
        System.out.println("MERGED JIRA STORIES");
        System.out.println("========================================");

        System.out.println(stories);

        // ========================================
        // STEP 2
        // GENERATE ERD JSON
        // ========================================

        String erdJson =
                callGemini(stories);

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED ERD JSON");
        System.out.println("========================================");

        System.out.println(erdJson);

        // ========================================
        // STEP 3
        // JSON -> ERD JAVA OBJECT
        // ========================================

        ObjectMapper mapper =
                new ObjectMapper();

        ErdModel erdModel =
                mapper.readValue(
                        erdJson,
                        ErdModel.class
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("ERD JAVA OBJECT CREATED");
        System.out.println("========================================");

        System.out.println(
                "Number of entities: "
                        + erdModel.getEntities().size()
        );

        System.out.println(
                "Number of relationships: "
                        + erdModel.getRelationships().size()
        );

        // ========================================
        // STEP 4
        // GENERATE ERD MERMAID
        // ========================================

        String mermaid =
                ErdMermaidGenerator.generate(
                        erdModel
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED MERMAID ERD");
        System.out.println("========================================");

        System.out.println(mermaid);

        // ========================================
        // STEP 5
        // GENERATE ERD IMAGE
        // ========================================

        ErdDiagramGenerator.generateDiagram(
                mermaid
        );

        // ========================================
        // STEP 6
        // GENERATE SYSTEM DESIGN JSON
        // ========================================

        String systemDesignJson =
                SystemDesignGenerator
                        .generateSystemDesign(
                                erdModel
                        );

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED SYSTEM DESIGN JSON");
        System.out.println("========================================");

        System.out.println(
                systemDesignJson
        );

        // ========================================
        // STEP 7
        // JSON -> SYSTEM DESIGN JAVA OBJECT
        // ========================================

        SystemDesignModel systemDesignModel =
                mapper.readValue(
                        systemDesignJson,
                        SystemDesignModel.class
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("SYSTEM DESIGN JAVA OBJECT CREATED");
        System.out.println("========================================");

        System.out.println(
                "Number of components: "
                        + systemDesignModel
                        .getComponents()
                        .size()
        );

        System.out.println(
                "Number of connections: "
                        + systemDesignModel
                        .getConnections()
                        .size()
        );

        // ========================================
        // STEP 8
        // GENERATE SYSTEM DESIGN MERMAID
        // ========================================

        String systemDesignMermaid =
                SystemDesignMermaidGenerator.generate(
                        systemDesignModel
                );

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATED SYSTEM DESIGN MERMAID");
        System.out.println("========================================");

        System.out.println(
                systemDesignMermaid
        );

        // ========================================
        // STEP 9
        // GENERATE SYSTEM DESIGN IMAGE
        // ========================================

        SystemDesignDiagramGenerator.generateDiagram(
                systemDesignMermaid
        );
    }
}