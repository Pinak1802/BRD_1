package org.pinak.JIRA_STORIES_TO_ERD;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ErdGenerator {

    private static final String JIRA_BASE_URL ="https://prd18022002.atlassian.net";

    private static final String JIRA_EMAIL ="prd18022002@gmail.com";

    private static final String JIRA_API_TOKEN ="ATATT3xFfGF00zKxGo2JgAU1L1Fe3K0GgIbXDQVb5DK14KpnphsvAN8aGbM_q1DqbKIxZMpbRpA_-XYokCMEZYtHWg_26zZ4igiG4zfwrPWOA4hiAVvFYaM-0zP5gV5w4bXtmms0YQSfOx4h7RXkvRBeUHQszA5pPKLs5R8l7nZye3Kq_6-x5TY=74B1929D";

    private static final String PROJECT_KEY = "SCRUM";

    public static void main(String[] args) throws Exception {

        System.out.println("========================================");
        System.out.println("DOWNLOADING JIRA USER STORIES");
        System.out.println("========================================");

        String stories = getJiraStories();

        System.out.println("\nJIRA STORIES:");
        System.out.println("----------------------------------------");
        System.out.println(stories);
        System.out.println("----------------------------------------");
    }

    public static String getJiraStories() throws Exception {

        String jql = "project = " + PROJECT_KEY +
                " AND issuetype = Story ORDER BY key";

        String encodedJql =
                java.net.URLEncoder.encode(
                        jql,
                        StandardCharsets.UTF_8
                );

        String url = JIRA_BASE_URL
                + "/rest/api/3/search/jql"
                + "?jql=" + encodedJql
                + "&fields=summary,description,issuetype"
                + "&maxResults=100";

        String auth = JIRA_EMAIL + ":" + JIRA_API_TOKEN;

        String encodedAuth = Base64.getEncoder()
                .encodeToString(
                        auth.getBytes(StandardCharsets.UTF_8)
                );

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println("Jira status: " + response.statusCode());

        if (response.statusCode() != 200) {
            System.out.println(response.body());
            throw new RuntimeException(
                    "Failed to download Jira stories"
            );
        }

        return mergeStories(response.body());
    }
    private static String mergeStories(String jiraJson) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(jiraJson);

        JsonNode issues = root.get("issues");

        StringBuilder mergedStories = new StringBuilder();

        mergedStories.append("===== JIRA USER STORIES =====\n\n");

        for (JsonNode issue : issues) {

            String key = issue.get("key").asText();

            JsonNode fields = issue.get("fields");

            String summary = fields.get("summary").asText();

            JsonNode description = fields.get("description");

            // Skip stories with empty descriptions
            if (description == null ||
                    description.get("content") == null ||
                    description.get("content").isEmpty()) {

                continue;
            }

            String descriptionText =
                    extractDescriptionText(description);

            // Skip if description contains no useful text
            if (descriptionText.isBlank()) {
                continue;
            }

            mergedStories.append("----------------------------------------\n");
            mergedStories.append("STORY: ")
                    .append(key)
                    .append("\n");

            mergedStories.append("TITLE: ")
                    .append(summary)
                    .append("\n\n");

            mergedStories.append(descriptionText)
                    .append("\n\n");
        }

        return mergedStories.toString();
    }
    private static String extractDescriptionText(JsonNode description) {

        StringBuilder text = new StringBuilder();

        JsonNode content = description.get("content");

        if (content == null) {
            return "";
        }

        for (JsonNode block : content) {

            JsonNode blockContent = block.get("content");

            if (blockContent == null) {
                continue;
            }

            for (JsonNode item : blockContent) {

                JsonNode textNode = item.get("text");

                if (textNode != null) {
                    text.append(textNode.asText());
                }
            }

            text.append("\n");
        }

        return text.toString().trim();
    }












    //todo calling the llm and getting the erd in return and saving it to a file

    public static String callGemini(String mergedStories) throws Exception {

//        String apiKey = System.getenv("GEMINI_API_KEY");
        String apiKey = "";

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "GEMINI_API_KEY environment variable is not set"
            );
        }

//        String model = "free/gpt-6-luna";
        String model = "gemini-3.5-flash-lite";

        String prompt = """
            You are an expert database architect.

            Analyze ALL of the Jira user stories below TOGETHER as one
            complete software system.

            Your task is to identify the database structure required
            by the requirements and produce an ERD model.

            IMPORTANT RULES:

            1. Analyze all stories together, NOT independently.
            2. Merge semantically identical entities.
               For example, if multiple stories mention "book",
               there must be only ONE Book entity.
            3. Do not create duplicate entities.
            4. Do not invent entities or attributes that are not
               reasonably supported by the user stories.
            5. Identify appropriate primary keys.
            6. Identify foreign keys where relationships require them.
            7. Identify relationships between entities.
            8. Identify relationship cardinality such as:
               1:1, 1:N, or M:N.
            9. Include important attributes explicitly mentioned
               in the stories.
            10. The final result must represent the COMPLETE system,
                not an ERD for an individual story.
            11. Return ONLY valid JSON.
            12. Do not include markdown code fences.
            13. Do not include explanations outside the JSON.

            Return this exact JSON structure:

            {
              "entities": [
                {
                  "name": "EntityName",
                  "attributes": [
                    {
                      "name": "attribute_name",
                      "type": "String",
                      "primaryKey": false,
                      "foreignKey": false
                    }
                  ]
                }
              ],
              "relationships": [
                {
                  "from": "EntityA",
                  "to": "EntityB",
                  "type": "relationship_name",
                  "cardinality": "1:N"
                }
              ]
            }

            Here are the Jira user stories:

            ----------------------------------------
            BEGIN USER STORIES
            ----------------------------------------

            """ + mergedStories + """

            ----------------------------------------
            END USER STORIES
            ----------------------------------------
            """;

        String requestBody = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": %s
                    }
                  ]
                }
              ]
            }
            """.formatted(toJsonString(prompt));

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
//                "https://api.apinex.bond/v1/chat/completions"
                        + model
                        + ":generateContent";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                requestBody,
                                StandardCharsets.UTF_8
                        )
                )
                .build();

        System.out.println();
        System.out.println("========================================");
        System.out.println("CALLING GEMINI FOR ERD ANALYSIS");
        System.out.println("========================================");

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println("Gemini status: "
                + response.statusCode());

        if (response.statusCode() != 200) {

            System.out.println("Gemini response:");
            System.out.println(response.body());

            throw new RuntimeException(
                    "Gemini API call failed"
            );
        }

        return extractGeminiText(response.body());
    }
    private static String extractGeminiText(String responseJson)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(responseJson);

        JsonNode candidates = root.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException(
                    "Gemini returned no candidates"
            );
        }

        JsonNode parts =
                candidates.get(0)
                        .get("content")
                        .get("parts");

        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException(
                    "Gemini returned no text"
            );
        }

        return parts.get(0)
                .get("text")
                .asText();
    }
    private static String toJsonString(String text) {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(text);
    }














}
