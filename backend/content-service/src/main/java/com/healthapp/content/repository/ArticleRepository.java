package com.healthapp.content.repository;

import com.healthapp.content.model.entity.Article;
import com.healthapp.content.model.enums.ArticleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ArticleRepository extends ReactiveMongoRepository<Article, String> {

    Mono<Article> findBySlug(String slug);

    Flux<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Flux<Article> findByCategoryIdAndStatusOrderByPublishedAtDesc(
            String categoryId, ArticleStatus status, Pageable pageable);

    Flux<Article> findByTagsContainingAndStatusOrderByPublishedAtDesc(
            String tag, ArticleStatus status, Pageable pageable);

    Flux<Article> findByAuthorDoctorIdAndStatusOrderByPublishedAtDesc(
            String doctorId, ArticleStatus status, Pageable pageable);

    Flux<Article> findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(
            ArticleStatus status, Pageable pageable);

    Flux<Article> findByIsEditorsPickTrueAndStatusOrderByPublishedAtDesc(
            ArticleStatus status, Pageable pageable);

    @Query("{ 'status': ?0, '$text': { '$search': ?1 } }")
    Flux<Article> searchByText(ArticleStatus status, String query, Pageable pageable);

    Mono<Long> countByStatus(ArticleStatus status);

    Mono<Long> countByCategoryIdAndStatus(String categoryId, ArticleStatus status);

    @Query(value = "{ 'status': 'PUBLISHED' }", sort = "{ 'stats.views': -1 }")
    Flux<Article> findTrendingArticles(Pageable pageable);
}
