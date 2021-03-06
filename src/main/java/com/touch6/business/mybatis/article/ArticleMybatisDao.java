package com.touch6.business.mybatis.article;


import com.touch6.business.entity.User;
import com.touch6.business.entity.article.Article;
import com.touch6.business.mybatis.common.MyBatisRepository;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuxl on 2015/5/20.
 */

@MyBatisRepository
public interface ArticleMybatisDao {
    int writeArticle(Article article);

    Article findById(String id);

    int updateArticle(Article article);

    List<Article> articleList(String uid);

    List<Article> findAll();

    int increaseCommentAmount(String articleId);

    void increaseApprovalAmount(String articleId);

    void increaseOpposeAmount(String articleId);

    int findApprovalAmountById(String objectId);

    int findOpposeAmountById(String objectId);

}
