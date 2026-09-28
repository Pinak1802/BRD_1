
package org.pinak.BV_BRD_JIRA_STORIES;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

public class FastMcpTest
{
    public record UserStory(
            String summary,
            String description,
            List<String> acceptanceCriteria
    ) {}

    public record UserStoryResponse(
            List<UserStory> stories
    ) {}

    public static void main(String[] args) throws Exception
    {

        System.out.println("Starting Java MCP Client...");

        // =========================================================
        // 1. START FASTMCP PYTHON SERVER
        // =========================================================

        ServerParameters serverParameters =
                ServerParameters.builder(
                                "C:\\Users\\VICTUS\\Downloads\\fastmcp-jira\\.venv\\Scripts\\python.exe"
                        )
                        .args(
                                "C:\\Users\\VICTUS\\Downloads\\fastmcp-jira\\server.py"
                        )
                        .build();

        StdioClientTransport transport =
                new StdioClientTransport(
                        serverParameters,
                        McpJsonDefaults.getMapper()
                );

        McpSyncClient client =
                McpClient.sync(transport)
                        .build();

        try {

            // =====================================================
            // 2. CONNECT TO MCP SERVER
            // =====================================================

            client.initialize();

            System.out.println("Connected to FastMCP!");

            // =====================================================
            // 3. DOWNLOAD BUSINESS VISION FROM JIRA
            // =====================================================

            System.out.println();
            System.out.println("Getting Business Vision from Jira...");
            System.out.println("--------------------------------");

            McpSchema.CallToolResult visionResult =
                    client.callTool(
                            new McpSchema.CallToolRequest(
                                    "download_business_vision",
                                    Map.of(
                                            "issue_key",
                                            "SCRUM-1"
                                    )
                            )
                    );

            String visionToolResult =
                    extractToolText(visionResult);

            System.out.println("MCP Response:");
            System.out.println(visionToolResult);

            if (visionToolResult.contains("Failed")
                    || visionToolResult.contains("not found")) {

                throw new RuntimeException(
                        "Business Vision could not be downloaded."
                );
            }

            // =====================================================
            // 4. GET BRD PROMPT FROM MCP
            // =====================================================

            System.out.println();
            System.out.println("Getting BRD Prompt...");
            System.out.println("--------------------------------");

            McpSchema.CallToolResult promptResult =
                    client.callTool(
                            new McpSchema.CallToolRequest(
                                    "get_brd_prompt",
                                    Map.of()
                            )
                    );

            String brdPrompt =
                    extractToolText(promptResult);

            System.out.println("BRD Prompt:");
            System.out.println(brdPrompt);

            // =====================================================
            // 5. LOCATE DOWNLOADED BUSINESS VISION PDF
            // =====================================================

            Path pdfPath =
                    Path.of(
                            "C:\\Users\\VICTUS\\Downloads\\fastmcp-jira\\Business_Vision.pdf"
                    );

            if (!Files.exists(pdfPath)) {

                throw new RuntimeException(
                        "Business_Vision.pdf was not found at: "
                                + pdfPath
                );
            }

            System.out.println();
            System.out.println("Business Vision PDF found:");
            System.out.println(pdfPath);

            // =====================================================
            // 6. EXTRACT TEXT FROM BUSINESS VISION PDF
            // =====================================================

            String businessVision =
                    extractPdfText(pdfPath);

            System.out.println();
            System.out.println("========================================");
            System.out.println("BUSINESS VISION TEXT");
            System.out.println("========================================");
            System.out.println(businessVision);

            if (businessVision.isBlank()) {

                throw new RuntimeException(
                        "Business Vision PDF contains no readable text."
                );
            }

            // =====================================================
            // 7. COMBINE PROMPT + BUSINESS VISION
            // =====================================================

            String userMessage =
                    brdPrompt
                            + "\n\n"
                            + "Business Vision:\n"
                            + businessVision;

            System.out.println();
            System.out.println("========================================");
            System.out.println("SENDING DATA TO LLM");
            System.out.println("========================================");

            // =====================================================
            // 8. GET LLM API KEY
            // =====================================================

            String apiKey =
            "";

            if (apiKey == null || apiKey.isBlank()) {

                throw new RuntimeException(
                        "LLM API key is not set."
                );
            }

            // =====================================================
            // 9. CREATE OBJECT MAPPER + HTTP CLIENT
            // =====================================================

            ObjectMapper mapper =
                    new ObjectMapper();

            HttpClient httpClient =
                    HttpClient.newHttpClient();


            //todo:JIRA CLIENT ADDED

            String jiraUrl =
                    "https://prd18022002.atlassian.net";

            String jiraEmail =
                    "prd18022002@gmail.com";

            String jiraApiToken =
                    "ATATT3xFfGF00zKxGo2JgAU1L1Fe3K0GgIbXDQVb5DK14KpnphsvAN8aGbM_q1DqbKIxZMpbRpA_-XYokCMEZYtHWg_26zZ4igiG4zfwrPWOA4hiAVvFYaM-0zP5gV5w4bXtmms0YQSfOx4h7RXkvRBeUHQszA5pPKLs5R8l7nZye3Kq_6-x5TY=74B1929D";
            JiraClient jiraClient =
                    new JiraClient(
                            jiraUrl,
                            jiraEmail,
                            jiraApiToken
                    );

            // =====================================================
            // 10. CREATE BRD JSON REQUEST
            // =====================================================

            String json =
                    mapper.createObjectNode()
                            .put(
                                    "model",
//                                    "gemini-3.5-flash-lite"
//                                    "gemini-3.8-flash"
//                                    "gemini-3.8-flash"
                                    "free/gpt-6-luna"
                            )


                            .set(
                                    "messages",
                                    mapper.createArrayNode()
                                            .add(
                                                    mapper.createObjectNode()
                                                            .put(
                                                                    "role",
                                                                    "user"
                                                            )
                                                            .put(
                                                                    "content",
                                                                    userMessage
                                                            )
                                            )
                            )
                            .toString();

            // =====================================================
            // 11. CALL LLM FOR BRD
            // =====================================================

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "https://api.apinex.bond/v1/chat/completions"
//                                            "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
                                    )
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            json
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            // =====================================================
            // 12. CHECK BRD LLM RESPONSE
            // =====================================================

            System.out.println();
            System.out.println(
                    "LLM HTTP STATUS: "
                            + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.out.println();
                System.out.println("LLM API ERROR:");
                System.out.println(response.body());

                throw new RuntimeException(
                        "LLM API request failed."
                );
            }

            // =====================================================
            // 13. PARSE BRD RESPONSE
            // =====================================================

            JsonNode root =
                    mapper.readTree(response.body());

            String brd =
                    root
                            .path("choices")
                            .path(0)
                            .path("message")
                            .path("content")
                            .asText();

            if (brd == null || brd.isBlank()) {

                throw new RuntimeException(
                        "LLM returned empty BRD."
                );
            }

            // =====================================================
            // 14. DISPLAY FINAL BRD
            // =====================================================

            System.out.println();
            System.out.println("========================================");
            System.out.println(
                    "GENERATED BUSINESS REQUIREMENTS DOCUMENT"
            );
            System.out.println("========================================");
            System.out.println();

            System.out.println(brd);

            System.out.println();
            System.out.println("========================================");
            System.out.println("BRD GENERATION COMPLETED");
            System.out.println("========================================");


            // =====================================================
            // 15. CALL SECOND LLM
            //     BRD -> USER STORIES
            // =====================================================

            System.out.println();
            System.out.println("========================================");
            System.out.println("GENERATING USER STORIES");
            System.out.println("========================================");

            String userStoryResponse =
                    generateUserStories(
                            brd,
                            mapper,
                            httpClient,
                            apiKey
                    );

            System.out.println();
            System.out.println("========================================");
            System.out.println("USER STORIES GENERATED");
            System.out.println("========================================");

            System.out.println(userStoryResponse);

            // =====================================================
            // 17. PARSE USER STORIES JSON
            // =====================================================

            UserStoryResponse storiesResponse =
                    mapper.readValue(
                            userStoryResponse,
                            UserStoryResponse.class
                    );

            System.out.println();
            System.out.println("========================================");
            System.out.println("UPLOADING USER STORIES TO JIRA");
            System.out.println("========================================");


// =====================================================
// 18. CREATE EACH USER STORY IN JIRA
// =====================================================

            for (UserStory story :
                    storiesResponse.stories()) {

                String jiraDescription =
                        story.description()
                                + "\n\n"
                                + "Acceptance Criteria:\n"
                                + String.join(
                                "\n",
                                story.acceptanceCriteria()
                        );

                System.out.println();
                System.out.println(
                        "Creating Jira Story: "
                                + story.summary()
                );

                String jiraResponse =
                        jiraClient.createIssue(
                                "SCRUM",
                                story.summary(),
                                jiraDescription
                        );

                System.out.println(
                        "Jira Response:"
                );

                System.out.println(jiraResponse);
            }


        } finally {

            // =====================================================
            // 16. CLOSE MCP CLIENT
            // =====================================================

            client.close();

            System.out.println();
            System.out.println("MCP client closed.");
        }
    }


    // =============================================================
    // METHOD 1: EXTRACT TEXT FROM MCP TOOL RESULT
    // =============================================================

    private static String extractToolText(
            McpSchema.CallToolResult result) {

        if (result == null || result.content() == null) {
            return "";
        }

        StringBuilder text =
                new StringBuilder();

        for (McpSchema.Content content :
                result.content()) {

            if (content instanceof McpSchema.TextContent textContent) {

                if (text.length() > 0) {
                    text.append("\n");
                }

                text.append(textContent.text());
            }
        }

        return text.toString();
    }


    // =============================================================
    // METHOD 2: EXTRACT TEXT FROM PDF
    // =============================================================

    private static String extractPdfText(
            Path pdfPath) throws IOException {

        try (PDDocument document =
                     Loader.loadPDF(pdfPath.toFile())) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }


    // =============================================================
    // METHOD 3: GENERATE USER STORIES FROM BRD
    // =============================================================

    private static String generateUserStories(
            String brd,
            ObjectMapper mapper,
            HttpClient httpClient,
            String apiKey
    ) throws Exception {

        // ---------------------------------------------------------
        // CREATE USER STORY PROMPT
        // ---------------------------------------------------------

        String prompt = """
                Convert the following Business Requirements into
                separate Jira user stories.

                Return ONLY valid JSON.

                Use exactly this structure:

                {
                  "stories": [
                    {
                      "summary": "...",
                      "description": "...",
                      "acceptanceCriteria": [
                        "...",
                        "..."
                      ]
                    }
                  ]
                }

                Rules:

                - Create one user story for each business requirement.
                - Do not combine unrelated requirements.
                - Summary must be short.
                - Description must follow this format:

                  As a <user>,
                  I want <goal>,
                  so that <benefit>.

                - Provide 2 to 5 acceptance criteria for each story.
                - Acceptance criteria must be specific and testable.
                - Do not add markdown.
                - Do not add explanations.
                - Return ONLY valid JSON.

                BRD:
                """ + brd;

        // ---------------------------------------------------------
        // CREATE JSON REQUEST
        // ---------------------------------------------------------

        String json =
                mapper.createObjectNode()
                        .put(
                                "model",
//                                "gemini-3.5-flash-lite"
                                "free/gpt-6-luna"
                        )
                        .set(
                                "messages",
                                mapper.createArrayNode()
                                        .add(
                                                mapper.createObjectNode()
                                                        .put(
                                                                "role",
                                                                "user"
                                                        )
                                                        .put(
                                                                "content",
                                                                prompt
                                                        )
                                        )
                        )
                        .toString();

        // ---------------------------------------------------------
        // CREATE HTTP REQUEST
        // ---------------------------------------------------------

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        "https://api.apinex.bond/v1/chat/completions"
//                                        "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
                                )
                        )
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        json
                                )
                        )
                        .build();

        // ---------------------------------------------------------
        // CALL LLM
        // ---------------------------------------------------------

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        // ---------------------------------------------------------
        // CHECK RESPONSE
        // ---------------------------------------------------------

        System.out.println();
        System.out.println(
                "User Story LLM status: "
                        + response.statusCode()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            System.out.println(
                    "User Story LLM ERROR:"
            );

            System.out.println(
                    response.body()
            );

            throw new RuntimeException(
                    "User Story LLM request failed."
            );
        }

        // ---------------------------------------------------------
        // PARSE LLM RESPONSE
        // ---------------------------------------------------------

        JsonNode root =
                mapper.readTree(response.body());

        String userStories =
                root
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();

        if (userStories == null
                || userStories.isBlank()) {

            throw new RuntimeException(
                    "LLM returned empty user stories."
            );
        }

        return userStories;
    }
}