package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Review;
import com.lifeequation.mapper.ReviewMapper;
import com.lifeequation.service.ReviewService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Override
    public Review createReview(Long userId, Review review) {
        review.setUserId(userId);
        this.save(review);
        return review;
    }

    @Override
    public Page<Review> getTargetReviews(String targetType, Long targetId, int page, int size) {
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getTargetType, targetType)
         .eq(Review::getTargetId, targetId)
         .orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public Page<Review> getUserReviews(Long userId, int page, int size) {
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getUserId, userId).orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public Map<String, Object> getTargetRatingSummary(String targetType, Long targetId) {
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getTargetType, targetType).eq(Review::getTargetId, targetId);
        List<Review> reviews = this.list(w);
        Map<String, Object> summary = new HashMap<>();
        summary.put("count", reviews.size());
        if (reviews.isEmpty()) {
            summary.put("avgRating", 0);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            summary.put("avgRating", Math.round(avg * 10.0) / 10.0);
        }
        return summary;
    }
}
