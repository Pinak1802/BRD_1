package org.pinak.ERD_SYSTEM_DESIGN;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class SystemDesignDiagramGenerator {

    public static void generateDiagram(String mermaid)
            throws Exception {

        System.out.println();
        System.out.println("========================================");
        System.out.println("GENERATING SYSTEM DESIGN IMAGE");
        System.out.println("========================================");

        // ========================================
        // ENCODE MERMAID
        // ========================================

        String encodedMermaid =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                mermaid.getBytes()
                        );

        // ========================================
        // MERMAID.INK URL
        // ========================================

        String url =
                "https://mermaid.ink/img/"
                        + encodedMermaid;

        System.out.println(
                "Requesting system design diagram..."
        );

        // ========================================
        // HTTP CLIENT
        // ========================================

        HttpClient client =
                HttpClient.newHttpClient();

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        // ========================================
        // SEND REQUEST
        // ========================================

        HttpResponse<byte[]> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofByteArray()
                );

        System.out.println(
                "Diagram HTTP status: "
                        + response.statusCode()
        );

        // ========================================
        // CHECK RESPONSE
        // ========================================

        if (response.statusCode() != 200) {

            System.out.println(
                    "Failed to generate system design image."
            );

            System.out.println(
                    "Response status: "
                            + response.statusCode()
            );

            throw new RuntimeException(
                    "System design diagram generation failed"
            );
        }

        // ========================================
        // SAVE IMAGE
        // ========================================

        Path outputPath =
                Path.of(
                        "system-design.png"
                );

        Files.write(
                outputPath,
                response.body()
        );

        // ========================================
        // SUCCESS
        // ========================================

        System.out.println();
        System.out.println(
                "System design image generated successfully!"
        );

        System.out.println(
                "File: "
                        + outputPath.toAbsolutePath()
        );

        System.out.println(
                "========================================"
        );
    }
}
