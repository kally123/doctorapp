package com.healthapp.content.repository;

import com.healthapp.content.model.entity.ArticleLike;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ArticleLikeRepository extends ReactiveMongoRepository<ArticleLike, String> {

    Mono<ArticleLike> findByArticleIdAndUserId(String articleId, String userId);

    Mono<Boolean> existsByArticleIdAndUserId(String articleId, String userId);

    Mono<Void> deleteByArticleIdAndUserId(String articleId, String userId);

    Mono<Long> countByArticleId(String articleId);
}
