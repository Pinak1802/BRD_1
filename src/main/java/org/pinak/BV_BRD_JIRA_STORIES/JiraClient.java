package org.pinak.BV_BRD_JIRA_STORIES;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class JiraClient {

    private final String jiraUrl;
    private final String email;
    private final String apiToken;

    private final HttpClient client;

    public JiraClient(String jiraUrl, String email, String apiToken) {
        this.jiraUrl = jiraUrl;
        this.email = email;
        this.apiToken = apiToken;
        this.client = HttpClient.newHttpClient();
    }

    public String createIssue(
            String projectKey,
            String summary,
            String description
    ) throws Exception {

        String credentials = email + ":" + apiToken;

        String auth = Base64.getEncoder()
                .encodeToString(credentials.getBytes());

        String json = """
                {
                  "fields": {
                    "project": {
                      "key": "%s"
                    },
                    "summary": "%s",
                    "description": {
                      "type": "doc",
                      "version": 1,
                      "content": [
                        {
                          "type": "paragraph",
                          "content": [
                            {
                              "type": "text",
                              "text": "%s"
                            }
                          ]
                        }
                      ]
                    },
                    "issuetype": {
                      "name": "Story"
                    }
                  }
                }
                """.formatted(
                escapeJson(projectKey),
                escapeJson(summary),
                escapeJson(description)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jiraUrl + "/rest/api/3/issue"))
                .header("Authorization", "Basic " + auth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request,
                        HttpResponse.BodyHandlers.ofString());

        System.out.println("Jira status: " + response.statusCode());
        System.out.println("Jira response: " + response.body());

        return response.body();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}