package com.gitserver.service;

import com.gitserver.dto.*;
import com.gitserver.entity.GitRepository;
import com.gitserver.entity.PullRequest;
import com.gitserver.entity.Review;
import com.gitserver.entity.Review.ReviewState;
import com.gitserver.entity.ReviewComment;
import com.gitserver.exception.PullRequestNotFoundException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.exception.ReviewNotFoundException;
import com.gitserver.repository.GitRepositoryJpaRepository;
import com.gitserver.repository.PullRequestRepository;
import com.gitserver.repository.ReviewCommentRepository;
import com.gitserver.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for code review operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final PullRequestRepository pullRequestRepository;
    private final GitRepositoryJpaRepository repositoryJpaRepository;

    /**
     * Create a new review on a pull request.
     */
    @Transactional
    public ReviewResponse createReview(String owner, String repoName, Integer prNumber, 
                                        String reviewerUsername, CreateReviewRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        ReviewState state = mapEventToState(request.getEvent());

        Review review = Review.builder()
                .pullRequestId(pullRequest.getId())
                .reviewerUsername(reviewerUsername)
                .body(request.getBody())
                .state(state)
                .commitId(request.getCommitId())
                .build();

        review = reviewRepository.save(review);
        log.info("Created review for PR #{} in repository {}/{}", prNumber, owner, repoName);

        return toResponse(review);
    }

    /**
     * Get all reviews for a pull request.
     */
    public List<ReviewResponse> getReviews(String owner, String repoName, Integer prNumber) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        return reviewRepository.findByPullRequestIdOrderByCreatedAtDesc(pullRequest.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific review.
     */
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        return toResponse(review);
    }

    /**
     * Dismiss a review.
     */
    @Transactional
    public ReviewResponse dismissReview(Long reviewId, String message) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.setState(ReviewState.DISMISSED);
        if (message != null) {
            review.setBody(review.getBody() + "\n\n---\nDismissal message: " + message);
        }

        review = reviewRepository.save(review);
        log.info("Dismissed review {}", reviewId);

        return toResponse(review);
    }

    /**
     * Add a comment to a review.
     */
    @Transactional
    public ReviewCommentResponse addComment(String owner, String repoName, Integer prNumber, 
                                             Long reviewId, String authorUsername, CreateReviewCommentRequest request) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        ReviewComment comment = ReviewComment.builder()
                .reviewId(review.getId())
                .pullRequestId(pullRequest.getId())
                .authorUsername(authorUsername)
                .body(request.getBody())
                .filePath(request.getFilePath())
                .lineNumber(request.getLineNumber())
                .commitId(request.getCommitId())
                .build();

        comment = reviewCommentRepository.save(comment);
        log.info("Added comment to review {} for PR #{} in repository {}/{}", reviewId, prNumber, owner, repoName);

        return toCommentResponse(comment);
    }

    /**
     * Get comments for a review.
     */
    public List<ReviewCommentResponse> getReviewComments(Long reviewId) {
        return reviewCommentRepository.findByReviewIdOrderByCreatedAtAsc(reviewId)
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all comments for a pull request.
     */
    public List<ReviewCommentResponse> getPullRequestComments(String owner, String repoName, Integer prNumber) {
        GitRepository repository = repositoryJpaRepository.findByOwnerAndName(owner, repoName)
                .orElseThrow(() -> new RepositoryNotFoundException(owner, repoName));

        PullRequest pullRequest = pullRequestRepository.findByRepositoryIdAndPrNumber(repository.getId(), prNumber)
                .orElseThrow(() -> new PullRequestNotFoundException(owner, repoName, prNumber));

        return reviewCommentRepository.findByPullRequestIdOrderByCreatedAtAsc(pullRequest.getId())
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    private ReviewState mapEventToState(String event) {
        return switch (event.toUpperCase()) {
            case "APPROVE" -> ReviewState.APPROVED;
            case "REQUEST_CHANGES" -> ReviewState.CHANGES_REQUESTED;
            case "COMMENT" -> ReviewState.COMMENTED;
            default -> ReviewState.PENDING;
        };
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .pullRequestId(review.getPullRequestId())
                .reviewer(review.getReviewerUsername())
                .body(review.getBody())
                .state(review.getState().name())
                .commitId(review.getCommitId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewCommentResponse toCommentResponse(ReviewComment comment) {
        return ReviewCommentResponse.builder()
                .id(comment.getId())
                .reviewId(comment.getReviewId())
                .pullRequestId(comment.getPullRequestId())
                .author(comment.getAuthorUsername())
                .body(comment.getBody())
                .filePath(comment.getFilePath())
                .lineNumber(comment.getLineNumber())
                .commitId(comment.getCommitId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
