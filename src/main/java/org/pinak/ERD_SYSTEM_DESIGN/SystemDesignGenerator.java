package org.pinak.ERD_SYSTEM_DESIGN;

import org.pinak.JIRA_STORIES_TO_ERD.ErdModel;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SystemDesignGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generateSystemDesign(ErdModel erdModel)
            throws Exception {

//        String apiKey = System.getenv("GEMINI_API_KEY");
        String apiKey = "";

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "GEMINI_API_KEY environment variable is not set"
            );
        }

        String erdJson = mapper.writeValueAsString(erdModel);

        String prompt = """
                You are an expert software architect.

                We already have a database ERD for a software system.

                Your task is to design a logical system architecture
                for the COMPLETE software system represented by this ERD.

                Analyze the entities and relationships together.

                IMPORTANT RULES:

                1. Create a complete logical system architecture.

                2. Do not create random components that are unrelated
                   to the requirements.

                3. Use the ERD entities and relationships to understand
                   the data responsibilities of the system.

                4. Identify appropriate application components such as:
                   - Client / Frontend
                   - API Gateway if appropriate
                   - Authentication / Authorization
                   - Backend services
                   - Database
                   - External services if required
                   - Message broker if asynchronous communication
                     is reasonably required

                5. Do not force microservices architecture.
                   Use a modular monolith when the requirements do not
                   justify multiple services.

                6. Every component must have:
                   - id
                   - name
                   - type
                   - technology
                   - description

                7. Every connection must have:
                   - from
                   - to
                   - protocol
                   - description

                8. The architecture must represent how the major
                   components communicate.

                9. Do not create database entities as separate
                   application services unless there is a reasonable
                   architectural reason.

                10. Use technologies only when they are supported by
                    the existing project context or are reasonable
                    defaults.

                11. Return ONLY valid JSON.

                12. Do NOT use markdown code fences.

                13. Do NOT provide explanations outside JSON.

                Return exactly this structure:

                {
                  "components": [
                    {
                      "id": "frontend",
                      "name": "Web Application",
                      "type": "Frontend",
                      "technology": "React",
                      "description": "User interface"
                    }
                  ],
                  "connections": [
                    {
                      "from": "frontend",
                      "to": "backend",
                      "protocol": "HTTPS",
                      "description": "REST API requests"
                    }
                  ]
                }

                ========================================
                ERD
                ========================================

                """ + erdJson + """

                ========================================
                END ERD
                ========================================
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

        String model = "gemini-3.5-flash-lite";
//        String model = "gemini-3.8-flash";
//        String model = "gemini-3.7-flash";


        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
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
        System.out.println("CALLING GEMINI FOR SYSTEM DESIGN");
        System.out.println("========================================");

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "Gemini system design status: "
                        + response.statusCode()
        );

        if (response.statusCode() != 200) {

            System.out.println(
                    "Gemini response:"
            );

            System.out.println(response.body());

            throw new RuntimeException(
                    "Gemini system design API call failed"
            );
        }

        return extractGeminiText(response.body());
    }

    private static String extractGeminiText(String response)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        var root = mapper.readTree(response);

        return root
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
    }

    private static String toJsonString(String value)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(value);
    }
}
