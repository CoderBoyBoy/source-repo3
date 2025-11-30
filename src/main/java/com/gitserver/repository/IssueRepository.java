package com.gitserver.repository;

import com.gitserver.entity.Issue;
import com.gitserver.entity.Issue.IssueState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Issue entity.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    List<Issue> findByRepositoryIdAndStateOrderByCreatedAtDesc(Long repositoryId, IssueState state);

    List<Issue> findByAuthorUsernameOrderByCreatedAtDesc(String authorUsername);

    List<Issue> findByAssigneeUsernameOrderByCreatedAtDesc(String assigneeUsername);

    Optional<Issue> findByRepositoryIdAndIssueNumber(Long repositoryId, Integer issueNumber);

    boolean existsByRepositoryIdAndIssueNumber(Long repositoryId, Integer issueNumber);

    @Query("SELECT MAX(i.issueNumber) FROM Issue i WHERE i.repositoryId = :repositoryId")
    Optional<Integer> findMaxIssueNumberByRepositoryId(@Param("repositoryId") Long repositoryId);

    long countByRepositoryIdAndState(Long repositoryId, IssueState state);
}
