package com.lifeequation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeequation.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {
    Page<Article> getArticlePage(int page, int size, String category, String keyword);
    Article getArticleDetail(Long id);
    List<Article> getHomeCarousel();
    List<Article> getLatestArticles(int limit);

    // Admin methods
    Page<Article> getArticlePageAdmin(int page, int size, String keyword, String status);
    void createArticle(Article article);
    void updateArticle(Long id, Article article);
    void deleteArticle(Long id);
    void togglePublish(Long id);
    void toggleTop(Long id);
}
