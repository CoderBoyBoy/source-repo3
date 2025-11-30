package com.gitserver.git;

import com.gitserver.dto.BranchInfo;
import com.gitserver.dto.CommitInfo;
import com.gitserver.dto.FileContent;
import com.gitserver.dto.FileInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JGitService.
 */
@SpringBootTest
class JGitServiceTest {

    @Autowired
    private JGitService jGitService;

    private static final String TEST_OWNER = "testuser";
    private static final String TEST_REPO = "jgittest";

    @BeforeEach
    void setUp() {
        // Clean up any existing test repository
        cleanupTestRepo();
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        cleanupTestRepo();
    }

    private void cleanupTestRepo() {
        try {
            jGitService.deleteRepository(TEST_OWNER, TEST_REPO);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testInitRepository() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        assertTrue(jGitService.repositoryExistsOnDisk(TEST_OWNER, TEST_REPO));
    }

    @Test
    void testGetBranches() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        List<BranchInfo> branches = jGitService.getBranches(TEST_OWNER, TEST_REPO);

        assertNotNull(branches);
        assertTrue(branches.size() >= 1);
        assertTrue(branches.stream().anyMatch(b -> b.getName().equals("main")));
    }

    @Test
    void testCreateBranch() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        BranchInfo newBranch = jGitService.createBranch(TEST_OWNER, TEST_REPO, "feature-test", "main");

        assertNotNull(newBranch);
        assertEquals("feature-test", newBranch.getName());

        // Verify branch exists
        List<BranchInfo> branches = jGitService.getBranches(TEST_OWNER, TEST_REPO);
        assertTrue(branches.stream().anyMatch(b -> b.getName().equals("feature-test")));
    }

    @Test
    void testGetCommits() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        List<CommitInfo> commits = jGitService.getCommits(TEST_OWNER, TEST_REPO, "main", 10);

        assertNotNull(commits);
        assertTrue(commits.size() >= 1);
        assertEquals("Initial commit", commits.get(0).getMessage().trim());
    }

    @Test
    void testGetFiles() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        List<FileInfo> files = jGitService.getFiles(TEST_OWNER, TEST_REPO, "main", "");

        assertNotNull(files);
        assertTrue(files.size() >= 1);
        assertTrue(files.stream().anyMatch(f -> f.getName().equals("README.md")));
    }

    @Test
    void testGetFileContent() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");

        FileContent content = jGitService.getFileContent(TEST_OWNER, TEST_REPO, "main", "README.md");

        assertNotNull(content);
        assertEquals("README.md", content.getName());
        assertTrue(content.getContent().contains(TEST_REPO));
    }

    @Test
    void testDeleteRepository() {
        jGitService.initRepository(TEST_OWNER, TEST_REPO, true, "Test description");
        assertTrue(jGitService.repositoryExistsOnDisk(TEST_OWNER, TEST_REPO));

        jGitService.deleteRepository(TEST_OWNER, TEST_REPO);
        assertFalse(jGitService.repositoryExistsOnDisk(TEST_OWNER, TEST_REPO));
    }
}
