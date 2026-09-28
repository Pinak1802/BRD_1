package org.pinak.BV_BRD_JIRA_STORIES;

public class JiraTest {

    public static void main(String[] args) throws Exception {

//        String jiraUrl = System.getenv("JIRA_URL");
//        String email = System.getenv("JIRA_EMAIL");
//        String token = System.getenv("JIRA_API_TOKEN");
        String jiraUrl="https://prd18022002.atlassian.net";
        String email="prd18022002@gmail.com";
        String token="ATATT3xFfGF08hZ4_9eqN3O827mOHKnHPZiDB_STwcC8XuYeiJvDVBrV5cIaPKGZN9QAAfpJLfetO-hk_eT_Dr48kGrQj_5AE90PztUV1z4KYk9RIL0qEfjsJpET2791o49VU48ncdRMpBwzHs_XMZlvT-4AW_jIiDk0poRruBXElVkRe10sOas=61C55895";


        JiraClient jiraClient =
                new JiraClient(jiraUrl, email, token);

        String response = jiraClient.createIssue(
                "SCRUM",
                "Allow students to search for books",
                """
                As a student,
                I want to search for books,
                so that I can find books available in the library.

                Acceptance Criteria:
                - Student can search using book name.
                - Matching books are displayed.
                - No-result message is displayed when appropriate.
                """
        );

        System.out.println(response);
    }
}