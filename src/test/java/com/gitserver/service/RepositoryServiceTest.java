package com.gitserver.service;

import com.gitserver.dto.CreateRepositoryRequest;
import com.gitserver.dto.RepositoryResponse;
import com.gitserver.exception.RepositoryAlreadyExistsException;
import com.gitserver.exception.RepositoryNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RepositoryService.
 */
@SpringBootTest
class RepositoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    private static final String TEST_OWNER = "testuser";
    private static final String TEST_REPO = "testrepo";

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
            if (repositoryService.repositoryExists(TEST_OWNER, TEST_REPO)) {
                repositoryService.deleteRepository(TEST_OWNER, TEST_REPO);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testCreateRepository() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository")
                .isPrivate(false)
                .initReadme(true)
                .build();

        RepositoryResponse response = repositoryService.createRepository(TEST_OWNER, request);

        assertNotNull(response);
        assertEquals(TEST_REPO, response.getName());
        assertEquals(TEST_OWNER, response.getOwner());
        assertEquals("Test repository", response.getDescription());
        assertFalse(response.isPrivate());
        assertNotNull(response.getCloneUrl());
    }

    @Test
    void testCreateDuplicateRepository() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository")
                .build();

        repositoryService.createRepository(TEST_OWNER, request);

        assertThrows(RepositoryAlreadyExistsException.class, () -> {
            repositoryService.createRepository(TEST_OWNER, request);
        });
    }

    @Test
    void testGetRepository() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository")
                .initReadme(true)
                .build();

        repositoryService.createRepository(TEST_OWNER, request);

        RepositoryResponse response = repositoryService.getRepository(TEST_OWNER, TEST_REPO);

        assertNotNull(response);
        assertEquals(TEST_REPO, response.getName());
        assertEquals(TEST_OWNER, response.getOwner());
    }

    @Test
    void testGetNonExistentRepository() {
        assertThrows(RepositoryNotFoundException.class, () -> {
            repositoryService.getRepository(TEST_OWNER, "nonexistent");
        });
    }

    @Test
    void testGetRepositoriesByOwner() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository")
                .initReadme(true)
                .build();

        repositoryService.createRepository(TEST_OWNER, request);

        List<RepositoryResponse> repos = repositoryService.getRepositoriesByOwner(TEST_OWNER);

        assertNotNull(repos);
        assertTrue(repos.size() >= 1);
        assertTrue(repos.stream().anyMatch(r -> r.getName().equals(TEST_REPO)));
    }

    @Test
    void testDeleteRepository() {
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository")
                .initReadme(true)
                .build();

        repositoryService.createRepository(TEST_OWNER, request);
        assertTrue(repositoryService.repositoryExists(TEST_OWNER, TEST_REPO));

        repositoryService.deleteRepository(TEST_OWNER, TEST_REPO);
        assertFalse(repositoryService.repositoryExists(TEST_OWNER, TEST_REPO));
    }
}
