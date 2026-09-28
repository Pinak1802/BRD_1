package org.pinak.JIRA_STORIES_TO_ERD;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class ErdDiagramGenerator {

    public static void generateDiagram(String mermaid) throws IOException, InterruptedException {

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATING ERD DIAGRAM");
        System.out.println("========================================");

        // Mermaid diagram URL
        String encodedMermaid =
                java.util.Base64.getEncoder()
                        .encodeToString(mermaid.getBytes());

        String url =
                "https://mermaid.ink/img/" + encodedMermaid;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        System.out.println("Diagram service status: "
                + response.statusCode());

        if (response.statusCode() == 200) {

            Path output =
                    Path.of("generated-erd.png");

            Files.write(
                    output,
                    response.body()
            );

            System.out.println();
            System.out.println("ERD DIAGRAM GENERATED SUCCESSFULLY");
            System.out.println("File: "
                    + output.toAbsolutePath());

        } else {

            System.out.println(
                    "Failed to generate ERD diagram."
            );
        }
    }
}
