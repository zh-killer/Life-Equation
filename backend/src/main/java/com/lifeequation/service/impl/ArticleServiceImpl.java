package com.lifeequation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeequation.entity.Article;
import com.lifeequation.mapper.ArticleMapper;
import com.lifeequation.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Override
    public Page<Article> getArticlePage(int page, int size, String category, String keyword) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(Article::getStatus, "PUBLISHED");
        if (category != null && !category.isBlank()) w.eq(Article::getCategory, category);
        if (keyword != null && !keyword.isBlank()) {
            w.like(Article::getTitle, keyword);
        }
        w.orderByDesc(Article::getIsTop).orderByDesc(Article::getPublishTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public Article getArticleDetail(Long id) {
        Article a = this.getById(id);
        if (a == null) throw new RuntimeException("文章不存在");
        a.setViewCount(a.getViewCount() + 1);
        this.updateById(a);
        return a;
    }

    @Override
    public List<Article> getHomeCarousel() {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(Article::getStatus, "PUBLISHED").eq(Article::getIsTop, 1)
                .orderByDesc(Article::getPublishTime).last("LIMIT 5");
        return this.list(w);
    }

    @Override
    public List<Article> getLatestArticles(int limit) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(Article::getStatus, "PUBLISHED")
                .orderByDesc(Article::getPublishTime).last("LIMIT " + limit);
        return this.list(w);
    }

    // ==================== Admin methods ====================

    @Override
    public Page<Article> getArticlePageAdmin(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(Article::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            w.like(Article::getTitle, keyword);
        }
        w.orderByDesc(Article::getIsTop).orderByDesc(Article::getCreateTime);
        return this.page(new Page<>(page, size), w);
    }

    @Override
    public void createArticle(Article article) {
        article.setViewCount(0);
        article.setIsTop(0);
        if (article.getStatus() == null) article.setStatus("DRAFT");
        this.save(article);
    }

    @Override
    public void updateArticle(Long id, Article article) {
        if (this.getById(id) == null) throw new RuntimeException("文章不存在");
        article.setId(id);
        this.updateById(article);
    }

    @Override
    public void deleteArticle(Long id) {
        if (this.getById(id) == null) throw new RuntimeException("文章不存在");
        this.removeById(id);
    }

    @Override
    public void togglePublish(Long id) {
        Article a = this.getById(id);
        if (a == null) throw new RuntimeException("文章不存在");
        if ("PUBLISHED".equals(a.getStatus())) {
            a.setStatus("DRAFT");
        } else {
            a.setStatus("PUBLISHED");
            a.setPublishTime(java.time.LocalDateTime.now());
        }
        this.updateById(a);
    }

    @Override
    public void toggleTop(Long id) {
        Article a = this.getById(id);
        if (a == null) throw new RuntimeException("文章不存在");
        a.setIsTop(a.getIsTop() != null && a.getIsTop() == 1 ? 0 : 1);
        this.updateById(a);
    }
}
