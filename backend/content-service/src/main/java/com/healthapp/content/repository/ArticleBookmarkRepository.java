package com.healthapp.content.repository;

import com.healthapp.content.model.entity.ArticleBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ArticleBookmarkRepository extends ReactiveMongoRepository<ArticleBookmark, String> {

    Mono<ArticleBookmark> findByArticleIdAndUserId(String articleId, String userId);

    Mono<Boolean> existsByArticleIdAndUserId(String articleId, String userId);

    Mono<Void> deleteByArticleIdAndUserId(String articleId, String userId);

    Flux<ArticleBookmark> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Mono<Long> countByUserId(String userId);
    
    Mono<Long> countByArticleId(String articleId);
}
