package com.healthapp.content.service;

import com.healthapp.content.dto.CategoryResponse;
import com.healthapp.content.model.entity.ArticleCategory;
import com.healthapp.content.repository.ArticleRepository;
import com.healthapp.content.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    /**
     * Get all active categories
     */
    public Flux<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByOrderAsc()
                .map(this::toResponse);
    }

    /**
     * Get root categories (no parent)
     */
    public Flux<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentCategoryIdIsNullAndIsActiveTrueOrderByOrderAsc()
                .map(this::toResponse);
    }

    /**
     * Get subcategories
     */
    public Flux<CategoryResponse> getSubcategories(String parentId) {
        return categoryRepository.findByParentCategoryIdAndIsActiveTrueOrderByOrderAsc(parentId)
                .map(this::toResponse);
    }

    /**
     * Get category by slug
     */
    public Mono<CategoryResponse> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::toResponse);
    }

    /**
     * Update article count for a category
     */
    public Mono<Void> updateArticleCount(String categoryId) {
        return articleRepository.countByCategoryIdAndStatus(
                        categoryId, 
                        com.healthapp.content.model.enums.ArticleStatus.PUBLISHED)
                .flatMap(count -> categoryRepository.findByCategoryId(categoryId)
                        .flatMap(category -> {
                            category.setArticleCount(count);
                            return categoryRepository.save(category);
                        }))
                .then();
    }

    private CategoryResponse toResponse(ArticleCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .slug(category.getSlug())
                .parentCategoryId(category.getParentCategoryId())
                .articleCount(category.getArticleCount())
                .order(category.getOrder())
                .build();
    }
}
