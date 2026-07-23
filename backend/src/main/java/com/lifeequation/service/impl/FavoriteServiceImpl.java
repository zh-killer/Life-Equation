package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Favorite;
import com.lifeequation.mapper.FavoriteMapper;
import com.lifeequation.service.FavoriteService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Override
    public Map<String, Object> toggleFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId)
         .eq(Favorite::getTargetType, targetType)
         .eq(Favorite::getTargetId, targetId);
        Favorite existing = this.getOne(w);
        Map<String, Object> result = new HashMap<>();
        if (existing != null) {
            this.removeById(existing.getId());
            result.put("favorited", false);
            result.put("message", "已取消收藏");
        } else {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setTargetType(targetType);
            f.setTargetId(targetId);
            this.save(f);
            result.put("favorited", true);
            result.put("message", "已收藏");
        }
        return result;
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId)
         .eq(Favorite::getTargetType, targetType)
         .eq(Favorite::getTargetId, targetId);
        return this.count(w) > 0;
    }

    @Override
    public Page<Favorite> getUserFavorites(Long userId, int page, int size, String targetType) {
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId);
        if (targetType != null && !targetType.isBlank()) w.eq(Favorite::getTargetType, targetType);
        w.orderByDesc(Favorite::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }
}
