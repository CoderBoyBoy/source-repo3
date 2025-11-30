package com.gitserver.controller;

import com.gitserver.dto.*;
import com.gitserver.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for code review operations.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/pulls/{prNumber}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Code review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a new review on a pull request")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        String reviewerUsername = authentication.getName();
        ReviewResponse response = reviewService.createReview(owner, repo, prNumber, reviewerUsername, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all reviews for a pull request")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber) {
        return ResponseEntity.ok(reviewService.getReviews(owner, repo, prNumber));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get a specific review")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReview(reviewId));
    }

    @PutMapping("/{reviewId}/dismiss")
    @Operation(summary = "Dismiss a review")
    public ResponseEntity<ReviewResponse> dismissReview(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @PathVariable Long reviewId,
            @RequestBody(required = false) String message) {
        return ResponseEntity.ok(reviewService.dismissReview(reviewId, message));
    }

    @PostMapping("/{reviewId}/comments")
    @Operation(summary = "Add a comment to a review")
    public ResponseEntity<ReviewCommentResponse> addComment(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewCommentRequest request,
            Authentication authentication) {
        String authorUsername = authentication.getName();
        ReviewCommentResponse response = reviewService.addComment(owner, repo, prNumber, reviewId, authorUsername, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reviewId}/comments")
    @Operation(summary = "Get comments for a review")
    public ResponseEntity<List<ReviewCommentResponse>> getReviewComments(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable Integer prNumber,
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewComments(reviewId));
    }
}
