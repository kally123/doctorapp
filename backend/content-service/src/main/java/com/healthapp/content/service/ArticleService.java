package com.healthapp.content.service;

import com.healthapp.content.dto.*;
import com.healthapp.content.model.entity.Article;
import com.healthapp.content.model.entity.ArticleCategory;
import com.healthapp.content.model.enums.ArticleStatus;
import com.healthapp.content.repository.*;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleLikeRepository likeRepository;
    private final ArticleBookmarkRepository bookmarkRepository;
    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    /**
     * Create a new article
     */
    @Transactional
    public Mono<ArticleResponse> createArticle(String userId, CreateArticleRequest request) {
        log.info("Creating article with title: {}", request.getTitle());

        String slug = generateSlug(request.getTitle());

        return categoryRepository.findByCategoryId(request.getCategoryId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Category not found")))
                .flatMap(category -> {
                    Article article = Article.builder()
                            .slug(slug)
                            .title(request.getTitle())
                            .subtitle(request.getSubtitle())
                            .content(request.getContent())
                            .excerpt(request.getExcerpt() != null ? request.getExcerpt() : 
                                    generateExcerpt(request.getContent()))
                            .featuredImage(toFeaturedImage(request.getFeaturedImage()))
                            .author(toAuthor(request.getAuthor()))
                            .category(Article.Category.builder()
                                    .id(category.getCategoryId())
                                    .name(category.getName())
                                    .build())
                            .tags(request.getTags())
                            .seo(toSeo(request.getSeo(), request.getTitle()))
                            .status(request.getStatus() != null ? request.getStatus() : ArticleStatus.DRAFT)
                            .scheduledPublishAt(request.getScheduledPublishAt())
                            .readTimeMinutes(calculateReadTime(request.getContent()))
                            .difficulty(request.getDifficulty())
                            .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                            .isPremium(request.getIsPremium() != null ? request.getIsPremium() : false)
                            .isEditorsPick(false)
                            .stats(Article.ArticleStats.builder()
                                    .views(0L)
                                    .uniqueViews(0L)
                                    .likes(0L)
                                    .shares(0L)
                                    .bookmarks(0L)
                                    .comments(0L)
                                    .avgReadTime(0.0)
                                    .build())
                            .createdBy(userId)
                            .version(1)
                            .build();

                    if (article.getStatus() == ArticleStatus.PUBLISHED) {
                        article.setPublishedAt(Instant.now());
                    }

                    return articleRepository.save(article);
                })
                .map(this::toResponse);
    }

    /**
     * Get article by slug
     */
    public Mono<ArticleResponse> getArticleBySlug(String slug, String userId) {
        return articleRepository.findBySlug(slug)
                .filter(a -> a.getStatus() == ArticleStatus.PUBLISHED)
                .flatMap(article -> enrichWithUserData(article, userId));
    }

    /**
     * Get article by ID
     */
    public Mono<ArticleResponse> getArticleById(String id, String userId) {
        return articleRepository.findById(id)
                .flatMap(article -> enrichWithUserData(article, userId));
    }

    /**
     * List published articles
     */
    public Flux<ArticleSummaryResponse> listArticles(ArticleFilter filter, int page, int size) {
        if (filter != null && filter.getQuery() != null && !filter.getQuery().isBlank()) {
            return articleRepository.searchByText(
                            ArticleStatus.PUBLISHED, 
                            filter.getQuery(), 
                            PageRequest.of(page, size))
                    .map(this::toSummaryResponse);
        }

        if (filter != null && filter.getCategoryId() != null) {
            return articleRepository.findByCategoryIdAndStatusOrderByPublishedAtDesc(
                            filter.getCategoryId(), 
                            ArticleStatus.PUBLISHED, 
                            PageRequest.of(page, size))
                    .map(this::toSummaryResponse);
        }

        if (filter != null && Boolean.TRUE.equals(filter.getIsFeatured())) {
            return articleRepository.findByIsFeaturedTrueAndStatusOrderByPublishedAtDesc(
                            ArticleStatus.PUBLISHED, 
                            PageRequest.of(page, size))
                    .map(this::toSummaryResponse);
        }

        return articleRepository.findByStatusOrderByPublishedAtDesc(
                        ArticleStatus.PUBLISHED, 
                        PageRequest.of(page, size))
                .map(this::toSummaryResponse);
    }

    /**
     * Get trending articles
     */
    public Flux<ArticleSummaryResponse> getTrendingArticles(int limit) {
        return articleRepository.findTrendingArticles(PageRequest.of(0, limit))
                .map(this::toSummaryResponse);
    }

    /**
     * Publish an article
     */
    @Transactional
    public Mono<ArticleResponse> publishArticle(String articleId) {
        return articleRepository.findById(articleId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Article not found")))
                .flatMap(article -> {
                    article.setStatus(ArticleStatus.PUBLISHED);
                    article.setPublishedAt(Instant.now());
                    return articleRepository.save(article);
                })
                .map(this::toResponse);
    }

    /**
     * Increment view count
     */
    public Mono<Void> recordView(String articleId) {
        return articleRepository.findById(articleId)
                .flatMap(article -> {
                    if (article.getStats() == null) {
                        article.setStats(Article.ArticleStats.builder().views(1L).build());
                    } else {
                        article.getStats().setViews(article.getStats().getViews() + 1);
                    }
                    return articleRepository.save(article);
                })
                .then();
    }

    /**
     * Like an article
     */
    @Transactional
    public Mono<Boolean> likeArticle(String articleId, String userId) {
        return likeRepository.existsByArticleIdAndUserId(articleId, userId)
                .flatMap(exists -> {
                    if (exists) {
                        // Unlike
                        return likeRepository.deleteByArticleIdAndUserId(articleId, userId)
                                .then(updateLikeCount(articleId))
                                .thenReturn(false);
                    } else {
                        // Like
                        return likeRepository.save(
                                        com.healthapp.content.model.entity.ArticleLike.builder()
                                                .articleId(articleId)
                                                .userId(userId)
                                                .build())
                                .then(updateLikeCount(articleId))
                                .thenReturn(true);
                    }
                });
    }

    /**
     * Bookmark an article
     */
    @Transactional
    public Mono<Boolean> bookmarkArticle(String articleId, String userId) {
        return bookmarkRepository.existsByArticleIdAndUserId(articleId, userId)
                .flatMap(exists -> {
                    if (exists) {
                        return bookmarkRepository.deleteByArticleIdAndUserId(articleId, userId)
                                .then(updateBookmarkCount(articleId))
                                .thenReturn(false);
                    } else {
                        return bookmarkRepository.save(
                                        com.healthapp.content.model.entity.ArticleBookmark.builder()
                                                .articleId(articleId)
                                                .userId(userId)
                                                .build())
                                .then(updateBookmarkCount(articleId))
                                .thenReturn(true);
                    }
                });
    }

    /**
     * Get user's bookmarked articles
     */
    public Flux<ArticleSummaryResponse> getUserBookmarks(String userId, int page, int size) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .flatMap(bookmark -> articleRepository.findById(bookmark.getArticleId()))
                .map(this::toSummaryResponse);
    }

    private Mono<Void> updateLikeCount(String articleId) {
        return likeRepository.countByArticleId(articleId)
                .flatMap(count -> articleRepository.findById(articleId)
                        .flatMap(article -> {
                            if (article.getStats() == null) {
                                article.setStats(Article.ArticleStats.builder().likes(count).build());
                            } else {
                                article.getStats().setLikes(count);
                            }
                            return articleRepository.save(article);
                        }))
                .then();
    }

    private Mono<Void> updateBookmarkCount(String articleId) {
        return bookmarkRepository.countByArticleId(articleId).defaultIfEmpty(0L)
                .flatMap(count -> articleRepository.findById(articleId)
                        .flatMap(article -> {
                            if (article.getStats() == null) {
                                article.setStats(Article.ArticleStats.builder().bookmarks(count).build());
                            } else {
                                article.getStats().setBookmarks(count);
                            }
                            return articleRepository.save(article);
                        }))
                .then();
    }

    private Mono<Long> countBookmarksByArticleId(String articleId) {
        // Count bookmarks for article would require a repository method
        return Mono.just(0L);
    }

    private Mono<ArticleResponse> enrichWithUserData(Article article, String userId) {
        if (userId == null) {
            return Mono.just(toResponse(article));
        }

        return Mono.zip(
                likeRepository.existsByArticleIdAndUserId(article.getId(), userId),
                bookmarkRepository.existsByArticleIdAndUserId(article.getId(), userId)
        ).map(tuple -> {
            ArticleResponse response = toResponse(article);
            response.setIsLiked(tuple.getT1());
            response.setIsBookmarked(tuple.getT2());
            return response;
        });
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateExcerpt(String content) {
        if (content == null) return "";
        String plainText = content.replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();
        return plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText;
    }

    private int calculateReadTime(String content) {
        if (content == null) return 1;
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200); // 200 words per minute
    }

    private Article.FeaturedImage toFeaturedImage(CreateArticleRequest.FeaturedImageDto dto) {
        if (dto == null) return null;
        return Article.FeaturedImage.builder()
                .url(dto.getUrl())
                .alt(dto.getAlt())
                .caption(dto.getCaption())
                .build();
    }

    private Article.Author toAuthor(CreateArticleRequest.AuthorDto dto) {
        if (dto == null) return null;
        return Article.Author.builder()
                .type(dto.getType())
                .doctorId(dto.getDoctorId())
                .name(dto.getName())
                .specialization(dto.getSpecialization())
                .credentials(dto.getCredentials())
                .build();
    }

    private Article.Seo toSeo(CreateArticleRequest.SeoDto dto, String title) {
        if (dto == null) {
            return Article.Seo.builder()
                    .metaTitle(title + " | HealthApp")
                    .build();
        }
        return Article.Seo.builder()
                .metaTitle(dto.getMetaTitle())
                .metaDescription(dto.getMetaDescription())
                .keywords(dto.getKeywords())
                .build();
    }

    private ArticleResponse toResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .content(article.getContent())
                .excerpt(article.getExcerpt())
                .featuredImage(article.getFeaturedImage() != null ?
                        ArticleResponse.FeaturedImageDto.builder()
                                .url(article.getFeaturedImage().getUrl())
                                .alt(article.getFeaturedImage().getAlt())
                                .caption(article.getFeaturedImage().getCaption())
                                .build() : null)
                .author(article.getAuthor() != null ?
                        ArticleResponse.AuthorDto.builder()
                                .type(article.getAuthor().getType() != null ? 
                                        article.getAuthor().getType().name() : null)
                                .doctorId(article.getAuthor().getDoctorId())
                                .name(article.getAuthor().getName())
                                .avatar(article.getAuthor().getAvatar())
                                .specialization(article.getAuthor().getSpecialization())
                                .credentials(article.getAuthor().getCredentials())
                                .build() : null)
                .category(article.getCategory() != null ?
                        ArticleResponse.CategoryDto.builder()
                                .id(article.getCategory().getId())
                                .name(article.getCategory().getName())
                                .build() : null)
                .tags(article.getTags())
                .status(article.getStatus())
                .publishedAt(article.getPublishedAt())
                .readTimeMinutes(article.getReadTimeMinutes())
                .difficulty(article.getDifficulty())
                .stats(article.getStats() != null ?
                        ArticleResponse.ArticleStatsDto.builder()
                                .views(article.getStats().getViews())
                                .likes(article.getStats().getLikes())
                                .shares(article.getStats().getShares())
                                .bookmarks(article.getStats().getBookmarks())
                                .comments(article.getStats().getComments())
                                .build() : null)
                .isFeatured(article.getIsFeatured())
                .isEditorsPick(article.getIsEditorsPick())
                .isPremium(article.getIsPremium())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    private ArticleSummaryResponse toSummaryResponse(Article article) {
        return ArticleSummaryResponse.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .excerpt(article.getExcerpt())
                .featuredImageUrl(article.getFeaturedImage() != null ? 
                        article.getFeaturedImage().getUrl() : null)
                .categoryName(article.getCategory() != null ? 
                        article.getCategory().getName() : null)
                .authorName(article.getAuthor() != null ? 
                        article.getAuthor().getName() : null)
                .authorAvatar(article.getAuthor() != null ? 
                        article.getAuthor().getAvatar() : null)
                .readTimeMinutes(article.getReadTimeMinutes())
                .views(article.getStats() != null ? article.getStats().getViews() : 0L)
                .likes(article.getStats() != null ? article.getStats().getLikes() : 0L)
                .isFeatured(article.getIsFeatured())
                .isEditorsPick(article.getIsEditorsPick())
                .publishedAt(article.getPublishedAt() != null ? 
                        article.getPublishedAt().toString() : null)
                .build();
    }
}
