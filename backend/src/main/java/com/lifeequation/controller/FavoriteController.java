package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Favorite;
import com.lifeequation.security.SecurityUtils;
import com.lifeequation.service.FavoriteService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        return Result.ok(favoriteService.toggleFavorite(userId, targetType, targetId));
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) String targetType) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Favorite> result = favoriteService.getUserFavorites(userId, page, size, targetType);
        return Result.ok(result);
    }

    @GetMapping("/check")
    public Result<?> check(@RequestParam String targetType, @RequestParam Long targetId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean favorited = favoriteService.isFavorited(userId, targetType, targetId);
        return Result.ok(Map.of("favorited", favorited));
    }
}
