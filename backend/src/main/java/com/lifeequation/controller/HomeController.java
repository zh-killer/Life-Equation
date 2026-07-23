package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    private final ArticleService articleService;

    public HomeController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/api/home")
    public Result<?> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("carousel", articleService.getHomeCarousel());
        data.put("latestArticles", articleService.getLatestArticles(6));
        data.put("quickEntries", new String[]{"diseases", "hospitals", "doctors", "articles"});
        return Result.ok(data);
    }
}
