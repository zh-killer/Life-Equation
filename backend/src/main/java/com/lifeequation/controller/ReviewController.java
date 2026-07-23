package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Review;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Result<?> create(@RequestBody Review review) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(reviewService.createReview(userId, review));
    }

    @GetMapping("/target")
    public Result<?> targetReviews(@RequestParam String targetType,
                                   @RequestParam Long targetId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Page<Review> reviews = reviewService.getTargetReviews(targetType, targetId, page, size);
        Map<String, Object> rating = reviewService.getTargetRatingSummary(targetType, targetId);
        return Result.ok(Map.of("reviews", reviews, "rating", rating));
    }

    @GetMapping("/my")
    public Result<?> myReviews(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(reviewService.getUserReviews(userId, page, size));
    }
}
