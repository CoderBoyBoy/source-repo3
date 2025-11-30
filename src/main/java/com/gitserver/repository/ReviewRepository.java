package com.gitserver.repository;

import com.gitserver.entity.Review;
import com.gitserver.entity.Review.ReviewState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Review entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);

    List<Review> findByReviewerUsernameOrderByCreatedAtDesc(String reviewerUsername);

    List<Review> findByPullRequestIdAndState(Long pullRequestId, ReviewState state);

    long countByPullRequestIdAndState(Long pullRequestId, ReviewState state);
}
