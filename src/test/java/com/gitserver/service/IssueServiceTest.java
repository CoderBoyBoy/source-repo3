package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.Issue;
import com.gitserver.exception.IssueNotFoundException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.IssueRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IssueService.
 */
@SpringBootTest
class IssueServiceTest {

    @Autowired
    private IssueService issueService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private GitRepositoryJpaRepository repositoryJpaRepository;

    @Autowired
    private IssueRepository issueRepository;

    private static final String TEST_OWNER = "testuser";
    private static final String TEST_REPO = "issuetestrepo";

    @BeforeEach
    void setUp() {
        cleanupTestData();
        // Create a test repository
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository for issues")
                .isPrivate(false)
                .initReadme(true)
                .build();
        repositoryService.createRepository(TEST_OWNER, request);
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            // Delete issues first
            repositoryJpaRepository.findByOwnerAndName(TEST_OWNER, TEST_REPO)
                    .ifPresent(repo -> {
                        List<Issue> issues = issueRepository.findByRepositoryIdOrderByCreatedAtDesc(repo.getId());
                        issueRepository.deleteAll(issues);
                    });
            
            // Delete repository
            if (repositoryService.repositoryExists(TEST_OWNER, TEST_REPO)) {
                repositoryService.deleteRepository(TEST_OWNER, TEST_REPO);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testCreateIssue() {
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Test Issue")
                .body("This is a test issue")
                .build();

        IssueResponse response = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        assertNotNull(response);
        assertEquals(1, response.getIssueNumber());
        assertEquals("Test Issue", response.getTitle());
        assertEquals("This is a test issue", response.getBody());
        assertEquals("OPEN", response.getState());
        assertEquals(TEST_OWNER, response.getAuthor());
    }

    @Test
    void testCreateIssueWithLabels() {
        Set<String> labels = new HashSet<>();
        labels.add("bug");
        labels.add("help wanted");

        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Bug Report")
                .body("This is a bug")
                .labels(labels)
                .build();

        IssueResponse response = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        assertNotNull(response);
        assertEquals("Bug Report", response.getTitle());
        assertNotNull(response.getLabels());
        assertTrue(response.getLabels().contains("bug"));
        assertTrue(response.getLabels().contains("help wanted"));
    }

    @Test
    void testGetIssue() {
        // Create an issue first
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Test Issue")
                .body("This is a test issue")
                .build();
        IssueResponse created = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Get the issue
        IssueResponse response = issueService.getIssue(TEST_OWNER, TEST_REPO, created.getIssueNumber());

        assertNotNull(response);
        assertEquals(created.getIssueNumber(), response.getIssueNumber());
        assertEquals("Test Issue", response.getTitle());
    }

    @Test
    void testGetNonExistentIssue() {
        assertThrows(IssueNotFoundException.class, () -> {
            issueService.getIssue(TEST_OWNER, TEST_REPO, 999);
        });
    }

    @Test
    void testGetIssues() {
        // Create multiple issues
        for (int i = 1; i <= 3; i++) {
            CreateIssueRequest request = CreateIssueRequest.builder()
                    .title("Issue " + i)
                    .body("Body " + i)
                    .build();
            issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);
        }

        List<IssueResponse> issues = issueService.getIssues(TEST_OWNER, TEST_REPO, null);

        assertNotNull(issues);
        assertEquals(3, issues.size());
    }

    @Test
    void testGetIssuesByState() {
        // Create some issues
        CreateIssueRequest request1 = CreateIssueRequest.builder()
                .title("Open Issue")
                .build();
        issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request1);

        CreateIssueRequest request2 = CreateIssueRequest.builder()
                .title("Closed Issue")
                .build();
        IssueResponse closedIssue = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request2);
        issueService.closeIssue(TEST_OWNER, TEST_REPO, closedIssue.getIssueNumber());

        // Get only open issues
        List<IssueResponse> openIssues = issueService.getIssues(TEST_OWNER, TEST_REPO, "OPEN");
        assertEquals(1, openIssues.size());
        assertEquals("Open Issue", openIssues.get(0).getTitle());

        // Get only closed issues
        List<IssueResponse> closedIssues = issueService.getIssues(TEST_OWNER, TEST_REPO, "CLOSED");
        assertEquals(1, closedIssues.size());
        assertEquals("Closed Issue", closedIssues.get(0).getTitle());
    }

    @Test
    void testUpdateIssue() {
        // Create an issue
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Original Title")
                .body("Original Body")
                .build();
        IssueResponse created = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Update the issue
        UpdateIssueRequest updateRequest = UpdateIssueRequest.builder()
                .title("Updated Title")
                .body("Updated Body")
                .build();
        IssueResponse updated = issueService.updateIssue(TEST_OWNER, TEST_REPO, created.getIssueNumber(), updateRequest);

        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Body", updated.getBody());
    }

    @Test
    void testCloseIssue() {
        // Create an issue
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Issue to Close")
                .build();
        IssueResponse created = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Close the issue
        IssueResponse closed = issueService.closeIssue(TEST_OWNER, TEST_REPO, created.getIssueNumber());

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getState());
        assertNotNull(closed.getClosedAt());
    }

    @Test
    void testReopenIssue() {
        // Create and close an issue
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Issue to Reopen")
                .build();
        IssueResponse created = issueService.createIssue(TEST_OWNER, TEST_REPO, TEST_OWNER, request);
        issueService.closeIssue(TEST_OWNER, TEST_REPO, created.getIssueNumber());

        // Reopen the issue
        IssueResponse reopened = issueService.reopenIssue(TEST_OWNER, TEST_REPO, created.getIssueNumber());

        assertNotNull(reopened);
        assertEquals("OPEN", reopened.getState());
    }

    @Test
    void testCreateIssueInNonExistentRepository() {
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title("Test Issue")
                .build();

        assertThrows(RepositoryNotFoundException.class, () -> {
            issueService.createIssue(TEST_OWNER, "nonexistent", TEST_OWNER, request);
        });
    }
}
