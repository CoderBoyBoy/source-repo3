package com.gitserver.repository;

import com.gitserver.entity.RepositoryPermission;
import com.gitserver.entity.RepositoryPermission.PermissionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for RepositoryPermission entity.
 */
@Repository
public interface RepositoryPermissionRepository extends JpaRepository<RepositoryPermission, Long> {

    List<RepositoryPermission> findByRepositoryId(Long repositoryId);

    List<RepositoryPermission> findByUsername(String username);

    Optional<RepositoryPermission> findByRepositoryIdAndUsername(Long repositoryId, String username);

    boolean existsByRepositoryIdAndUsername(Long repositoryId, String username);

    List<RepositoryPermission> findByRepositoryIdAndPermission(Long repositoryId, PermissionLevel permission);

    void deleteByRepositoryIdAndUsername(Long repositoryId, String username);
}
