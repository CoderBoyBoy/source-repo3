package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.PullRequest;
import com.gitserver.exception.PullRequestNotFoundException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.git.JGitService;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.PullRequestRepository;
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
 * Tests for PullRequestService.
 */
@SpringBootTest
class PullRequestServiceTest {

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private JGitService jGitService;

    @Autowired
    private GitRepositoryJpaRepository repositoryJpaRepository;

    @Autowired
    private PullRequestRepository pullRequestRepository;

    private static final String TEST_OWNER = "testuser";
    private static final String TEST_REPO = "prtestrepo";
    private static final String FEATURE_BRANCH = "feature-test";

    @BeforeEach
    void setUp() {
        cleanupTestData();
        // Create a test repository with initial commit
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository for PRs")
                .isPrivate(false)
                .initReadme(true)
                .build();
        repositoryService.createRepository(TEST_OWNER, request);

        // Create a feature branch
        jGitService.createBranch(TEST_OWNER, TEST_REPO, FEATURE_BRANCH, "main");
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            // Delete pull requests first
            repositoryJpaRepository.findByOwnerAndName(TEST_OWNER, TEST_REPO)
                    .ifPresent(repo -> {
                        List<PullRequest> prs = pullRequestRepository.findByRepositoryIdOrderByCreatedAtDesc(repo.getId());
                        pullRequestRepository.deleteAll(prs);
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
    void testCreatePullRequest() {
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("Test PR")
                .body("This is a test PR")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .build();

        PullRequestResponse response = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        assertNotNull(response);
        assertEquals(1, response.getPrNumber());
        assertEquals("Test PR", response.getTitle());
        assertEquals("This is a test PR", response.getBody());
        assertEquals("OPEN", response.getState());
        assertEquals(TEST_OWNER, response.getAuthor());
        assertEquals(FEATURE_BRANCH, response.getHeadBranch());
        assertEquals("main", response.getBaseBranch());
    }

    @Test
    void testCreatePullRequestWithReviewers() {
        Set<String> reviewers = new HashSet<>();
        reviewers.add("reviewer1");
        reviewers.add("reviewer2");

        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("PR with Reviewers")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .reviewers(reviewers)
                .build();

        PullRequestResponse response = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        assertNotNull(response);
        assertNotNull(response.getReviewers());
        assertTrue(response.getReviewers().contains("reviewer1"));
        assertTrue(response.getReviewers().contains("reviewer2"));
    }

    @Test
    void testGetPullRequest() {
        // Create a PR first
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("Test PR")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .build();
        PullRequestResponse created = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Get the PR
        PullRequestResponse response = pullRequestService.getPullRequest(TEST_OWNER, TEST_REPO, created.getPrNumber());

        assertNotNull(response);
        assertEquals(created.getPrNumber(), response.getPrNumber());
        assertEquals("Test PR", response.getTitle());
    }

    @Test
    void testGetNonExistentPullRequest() {
        assertThrows(PullRequestNotFoundException.class, () -> {
            pullRequestService.getPullRequest(TEST_OWNER, TEST_REPO, 999);
        });
    }

    @Test
    void testGetPullRequests() {
        // Create multiple PRs (need different branches)
        jGitService.createBranch(TEST_OWNER, TEST_REPO, "feature-1", "main");
        jGitService.createBranch(TEST_OWNER, TEST_REPO, "feature-2", "main");

        CreatePullRequestRequest request1 = CreatePullRequestRequest.builder()
                .title("PR 1")
                .headBranch("feature-1")
                .baseBranch("main")
                .build();
        pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request1);

        CreatePullRequestRequest request2 = CreatePullRequestRequest.builder()
                .title("PR 2")
                .headBranch("feature-2")
                .baseBranch("main")
                .build();
        pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request2);

        List<PullRequestResponse> prs = pullRequestService.getPullRequests(TEST_OWNER, TEST_REPO, null);

        assertNotNull(prs);
        assertEquals(2, prs.size());
    }

    @Test
    void testUpdatePullRequest() {
        // Create a PR
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("Original Title")
                .body("Original Body")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .build();
        PullRequestResponse created = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Update the PR
        UpdatePullRequestRequest updateRequest = UpdatePullRequestRequest.builder()
                .title("Updated Title")
                .body("Updated Body")
                .build();
        PullRequestResponse updated = pullRequestService.updatePullRequest(TEST_OWNER, TEST_REPO, created.getPrNumber(), updateRequest);

        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Body", updated.getBody());
    }

    @Test
    void testClosePullRequest() {
        // Create a PR
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("PR to Close")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .build();
        PullRequestResponse created = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request);

        // Close the PR
        PullRequestResponse closed = pullRequestService.closePullRequest(TEST_OWNER, TEST_REPO, created.getPrNumber());

        assertNotNull(closed);
        assertEquals("CLOSED", closed.getState());
        assertNotNull(closed.getClosedAt());
    }

    @Test
    void testCreatePullRequestInNonExistentRepository() {
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title("Test PR")
                .headBranch(FEATURE_BRANCH)
                .baseBranch("main")
                .build();

        assertThrows(RepositoryNotFoundException.class, () -> {
            pullRequestService.createPullRequest(TEST_OWNER, "nonexistent", TEST_OWNER, request);
        });
    }

    @Test
    void testCountPullRequests() {
        // Create and close some PRs
        jGitService.createBranch(TEST_OWNER, TEST_REPO, "feature-count-1", "main");
        jGitService.createBranch(TEST_OWNER, TEST_REPO, "feature-count-2", "main");

        CreatePullRequestRequest request1 = CreatePullRequestRequest.builder()
                .title("Open PR")
                .headBranch("feature-count-1")
                .baseBranch("main")
                .build();
        pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request1);

        CreatePullRequestRequest request2 = CreatePullRequestRequest.builder()
                .title("Closed PR")
                .headBranch("feature-count-2")
                .baseBranch("main")
                .build();
        PullRequestResponse pr2 = pullRequestService.createPullRequest(TEST_OWNER, TEST_REPO, TEST_OWNER, request2);
        pullRequestService.closePullRequest(TEST_OWNER, TEST_REPO, pr2.getPrNumber());

        GitRepository repo = repositoryJpaRepository.findByOwnerAndName(TEST_OWNER, TEST_REPO).get();

        assertEquals(1, pullRequestService.countOpenPullRequests(repo.getId()));
        assertEquals(1, pullRequestService.countClosedPullRequests(repo.getId()));
    }
}
