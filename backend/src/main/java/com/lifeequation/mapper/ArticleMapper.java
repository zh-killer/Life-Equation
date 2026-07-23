package com.lifeequation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeequation.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
