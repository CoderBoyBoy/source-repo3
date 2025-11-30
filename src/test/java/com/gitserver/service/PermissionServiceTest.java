package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.RepositoryPermission.PermissionLevel;
import com.gitserver.exception.PermissionDeniedException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.exception.UserNotFoundException;
import com.gitserver.repository.RepositoryPermissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PermissionService.
 */
@SpringBootTest
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private RepositoryPermissionRepository permissionRepository;

    private static final String TEST_OWNER = "admin";
    private static final String TEST_REPO = "permissiontestrepo";
    private static final String TEST_COLLABORATOR = "testcollaborator";

    @BeforeEach
    void setUp() {
        cleanupTestData();
        // Create a test repository
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(TEST_REPO)
                .description("Test repository for permissions")
                .isPrivate(true)
                .initReadme(true)
                .build();
        repositoryService.createRepository(TEST_OWNER, request);

        // Create a test collaborator user
        try {
            CreateUserRequest userRequest = CreateUserRequest.builder()
                    .username(TEST_COLLABORATOR)
                    .email(TEST_COLLABORATOR + "@test.com")
                    .password("password123")
                    .build();
            userService.createUser(userRequest);
        } catch (Exception e) {
            // User might already exist
        }
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        try {
            // Delete permissions first
            permissionRepository.deleteAll();
            
            // Delete repository
            if (repositoryService.repositoryExists(TEST_OWNER, TEST_REPO)) {
                repositoryService.deleteRepository(TEST_OWNER, TEST_REPO);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testAddCollaborator() {
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();

        PermissionResponse response = permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        assertNotNull(response);
        assertEquals(TEST_COLLABORATOR, response.getUsername());
        assertEquals("READ", response.getPermission());
    }

    @Test
    void testAddCollaboratorWithWritePermission() {
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("WRITE")
                .build();

        PermissionResponse response = permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        assertNotNull(response);
        assertEquals("WRITE", response.getPermission());
    }

    @Test
    void testAddCollaboratorWithAdminPermission() {
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("ADMIN")
                .build();

        PermissionResponse response = permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        assertNotNull(response);
        assertEquals("ADMIN", response.getPermission());
    }

    @Test
    void testUpdateCollaboratorPermission() {
        // Add collaborator first
        PermissionRequest addRequest = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, addRequest);

        // Update permission
        PermissionRequest updateRequest = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("WRITE")
                .build();
        PermissionResponse response = permissionService.addCollaborator(TEST_OWNER, TEST_REPO, updateRequest);

        assertNotNull(response);
        assertEquals("WRITE", response.getPermission());
    }

    @Test
    void testGetCollaborators() {
        // Add a collaborator
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        List<PermissionResponse> collaborators = permissionService.getCollaborators(TEST_OWNER, TEST_REPO);

        assertNotNull(collaborators);
        assertEquals(1, collaborators.size());
        assertEquals(TEST_COLLABORATOR, collaborators.get(0).getUsername());
    }

    @Test
    void testGetCollaborator() {
        // Add a collaborator
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        PermissionResponse response = permissionService.getCollaborator(TEST_OWNER, TEST_REPO, TEST_COLLABORATOR);

        assertNotNull(response);
        assertEquals(TEST_COLLABORATOR, response.getUsername());
        assertEquals("READ", response.getPermission());
    }

    @Test
    void testRemoveCollaborator() {
        // Add a collaborator
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        // Remove the collaborator
        permissionService.removeCollaborator(TEST_OWNER, TEST_REPO, TEST_COLLABORATOR);

        // Verify removed
        assertThrows(PermissionDeniedException.class, () -> {
            permissionService.getCollaborator(TEST_OWNER, TEST_REPO, TEST_COLLABORATOR);
        });
    }

    @Test
    void testOwnerHasAdminPermission() {
        boolean hasAdmin = permissionService.canAdmin(TEST_OWNER, TEST_REPO, TEST_OWNER);
        assertTrue(hasAdmin);
    }

    @Test
    void testOwnerHasWritePermission() {
        boolean canWrite = permissionService.canWrite(TEST_OWNER, TEST_REPO, TEST_OWNER);
        assertTrue(canWrite);
    }

    @Test
    void testCollaboratorWithReadCannotWrite() {
        // Add collaborator with READ permission
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        boolean canWrite = permissionService.canWrite(TEST_OWNER, TEST_REPO, TEST_COLLABORATOR);
        assertFalse(canWrite);
    }

    @Test
    void testCollaboratorWithWriteCanWrite() {
        // Add collaborator with WRITE permission
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("WRITE")
                .build();
        permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);

        boolean canWrite = permissionService.canWrite(TEST_OWNER, TEST_REPO, TEST_COLLABORATOR);
        assertTrue(canWrite);
    }

    @Test
    void testAddNonExistentUser() {
        PermissionRequest request = PermissionRequest.builder()
                .username("nonexistentuser")
                .permission("READ")
                .build();

        assertThrows(UserNotFoundException.class, () -> {
            permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);
        });
    }

    @Test
    void testAddCollaboratorToNonExistentRepo() {
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_COLLABORATOR)
                .permission("READ")
                .build();

        assertThrows(RepositoryNotFoundException.class, () -> {
            permissionService.addCollaborator(TEST_OWNER, "nonexistent", request);
        });
    }

    @Test
    void testCannotAddOwnerAsCollaborator() {
        PermissionRequest request = PermissionRequest.builder()
                .username(TEST_OWNER)
                .permission("READ")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            permissionService.addCollaborator(TEST_OWNER, TEST_REPO, request);
        });
    }
}
