package com.healthapp.content.controller;

import com.healthapp.content.dto.*;
import com.healthapp.content.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Articles", description = "Health article management APIs")
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new article")
    public Mono<ArticleResponse> createArticle(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateArticleRequest request) {
        return articleService.createArticle(userId, request);
    }

    @GetMapping
    @Operation(summary = "List published articles")
    public Flux<ArticleSummaryResponse> listArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ArticleFilter filter = ArticleFilter.builder()
                .query(query)
                .categoryId(categoryId)
                .isFeatured(featured)
                .build();
        
        return articleService.listArticles(filter, page, size);
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending articles")
    public Flux<ArticleSummaryResponse> getTrendingArticles(
            @RequestParam(defaultValue = "10") int limit) {
        return articleService.getTrendingArticles(limit);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get article by slug")
    public Mono<ArticleResponse> getArticleBySlug(
            @PathVariable String slug,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return articleService.getArticleBySlug(slug, userId)
                .doOnSuccess(article -> {
                    if (article != null) {
                        articleService.recordView(article.getId()).subscribe();
                    }
                });
    }

    @PostMapping("/{articleId}/like")
    @Operation(summary = "Like or unlike an article")
    public Mono<LikeResponse> likeArticle(
            @PathVariable String articleId,
            @RequestHeader("X-User-Id") String userId) {
        return articleService.likeArticle(articleId, userId)
                .map(isLiked -> new LikeResponse(isLiked));
    }

    @PostMapping("/{articleId}/bookmark")
    @Operation(summary = "Bookmark or unbookmark an article")
    public Mono<BookmarkResponse> bookmarkArticle(
            @PathVariable String articleId,
            @RequestHeader("X-User-Id") String userId) {
        return articleService.bookmarkArticle(articleId, userId)
                .map(isBookmarked -> new BookmarkResponse(isBookmarked));
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "Get user's bookmarked articles")
    public Flux<ArticleSummaryResponse> getUserBookmarks(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return articleService.getUserBookmarks(userId, page, size);
    }

    @PutMapping("/{articleId}/publish")
    @Operation(summary = "Publish a draft article")
    public Mono<ArticleResponse> publishArticle(@PathVariable String articleId) {
        return articleService.publishArticle(articleId);
    }

    public record LikeResponse(boolean liked) {}
    public record BookmarkResponse(boolean bookmarked) {}
}
