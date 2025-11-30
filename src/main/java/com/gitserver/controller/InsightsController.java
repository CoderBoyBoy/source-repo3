package com.gitserver.controller;

import com.gitserver.dto.RepositoryInsights;
import com.gitserver.service.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for repository insights/statistics.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "Repository insights and statistics APIs")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping
    @Operation(summary = "Get comprehensive insights for a repository")
    public ResponseEntity<RepositoryInsights> getRepositoryInsights(
            @PathVariable String owner,
            @PathVariable String repo) {
        return ResponseEntity.ok(insightsService.getRepositoryInsights(owner, repo));
    }
}
