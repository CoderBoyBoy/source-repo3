package com.gitserver.repository;

import com.gitserver.entity.PullRequest;
import com.gitserver.entity.PullRequest.PullRequestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for PullRequest entity.
 */
@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    List<PullRequest> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    List<PullRequest> findByRepositoryIdAndStateOrderByCreatedAtDesc(Long repositoryId, PullRequestState state);

    List<PullRequest> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);

    Optional<PullRequest> findByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);

    boolean existsByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);

    @Query("SELECT MAX(p.prNumber) FROM PullRequest p WHERE p.repositoryId = :repositoryId")
    Optional<Integer> findMaxPrNumberByRepositoryId(@Param("repositoryId") Long repositoryId);

    long countByRepositoryIdAndState(Long repositoryId, PullRequestState state);

    @Query("SELECT p FROM PullRequest p JOIN p.reviewers r WHERE r = :username ORDER BY p.createdAt DESC")
    List<PullRequest> findByReviewer(@Param("username") String username);
}
