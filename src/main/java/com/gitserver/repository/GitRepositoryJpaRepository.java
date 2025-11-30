package com.gitserver.repository;

import com.gitserver.entity.GitRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for GitRepository entity.
 */
@Repository
public interface GitRepositoryJpaRepository extends JpaRepository<GitRepository, Long> {

    Optional<GitRepository> findByName(String name);

    Optional<GitRepository> findByOwnerAndName(String owner, String name);

    List<GitRepository> findByOwner(String owner);

    List<GitRepository> findByIsPrivate(boolean isPrivate);

    boolean existsByName(String name);

    boolean existsByOwnerAndName(String owner, String name);
}
