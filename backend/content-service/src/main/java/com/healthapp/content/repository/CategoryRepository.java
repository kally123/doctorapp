package com.healthapp.content.repository;

import com.healthapp.content.model.entity.ArticleCategory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<ArticleCategory, String> {

    Mono<ArticleCategory> findByCategoryId(String categoryId);

    Mono<ArticleCategory> findBySlug(String slug);

    Flux<ArticleCategory> findByIsActiveTrueOrderByOrderAsc();

    Flux<ArticleCategory> findByParentCategoryIdAndIsActiveTrueOrderByOrderAsc(String parentId);

    Flux<ArticleCategory> findByParentCategoryIdIsNullAndIsActiveTrueOrderByOrderAsc();
}
