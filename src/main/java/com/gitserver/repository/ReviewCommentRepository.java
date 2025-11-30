package com.gitserver.repository;

import com.gitserver.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for ReviewComment entity.
 */
@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    List<ReviewComment> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    List<ReviewComment> findByPullRequestIdOrderByCreatedAtAsc(Long pullRequestId);

    List<ReviewComment> findByPullRequestIdAndFilePath(Long pullRequestId, String filePath);

    long countByPullRequestId(Long pullRequestId);
}
