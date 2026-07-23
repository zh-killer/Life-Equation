package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Review;
import java.util.Map;

public interface ReviewService extends IService<Review> {
    Review createReview(Long userId, Review review);
    Page<Review> getTargetReviews(String targetType, Long targetId, int page, int size);
    Page<Review> getUserReviews(Long userId, int page, int size);
    Map<String, Object> getTargetRatingSummary(String targetType, Long targetId);
}
