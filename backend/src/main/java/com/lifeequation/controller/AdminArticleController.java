package com.lifeequation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Article;
import com.lifeequation.service.ArticleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status) {
        Page<Article> result = articleService.getArticlePageAdmin(page, size, keyword, status);
        return Result.ok(result);
    }

    @PostMapping
    public Result<?> create(@RequestBody Article article) {
        articleService.createArticle(article);
        return Result.ok("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Article article) {
        articleService.updateArticle(id, article);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.ok("删除成功");
    }

    @PutMapping("/{id}/publish")
    public Result<?> togglePublish(@PathVariable Long id) {
        articleService.togglePublish(id);
        return Result.ok("状态已切换");
    }

    @PutMapping("/{id}/top")
    public Result<?> toggleTop(@PathVariable Long id) {
        articleService.toggleTop(id);
        return Result.ok("置顶状态已切换");
    }
}
