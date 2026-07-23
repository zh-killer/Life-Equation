package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Favorite;
import java.util.Map;

public interface FavoriteService extends IService<Favorite> {
    Map<String, Object> toggleFavorite(Long userId, String targetType, Long targetId);
    boolean isFavorited(Long userId, String targetType, Long targetId);
    Page<Favorite> getUserFavorites(Long userId, int page, int size, String targetType);
}
