package com.healthapp.content.dto;

import com.healthapp.content.model.enums.ArticleStatus;
import com.healthapp.content.model.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private String id;
    private String slug;

    // Content
    private String title;
    private String subtitle;
    private String content;
    private String excerpt;

    // Media
    private FeaturedImageDto featuredImage;

    // Author
    private AuthorDto author;

    // Categorization
    private CategoryDto category;
    private CategoryDto subcategory;
    private List<String> tags;

    // SEO
    private SeoDto seo;

    // Publishing
    private ArticleStatus status;
    private Instant publishedAt;

    // Reading
    private Integer readTimeMinutes;
    private Difficulty difficulty;

    // Engagement
    private ArticleStatsDto stats;

    // Flags
    private Boolean isFeatured;
    private Boolean isEditorsPick;
    private Boolean isPremium;

    // User engagement (for authenticated users)
    private Boolean isLiked;
    private Boolean isBookmarked;

    // Related
    private List<ArticleSummaryDto> relatedArticles;

    // Medical review
    private MedicalReviewDto medicalReview;

    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturedImageDto {
        private String url;
        private String alt;
        private String caption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private String type;
        private String doctorId;
        private String name;
        private String avatar;
        private String specialization;
        private String credentials;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private String id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeoDto {
        private String metaTitle;
        private String metaDescription;
        private List<String> keywords;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleStatsDto {
        private Long views;
        private Long likes;
        private Long shares;
        private Long bookmarks;
        private Long comments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalReviewDto {
        private String reviewedBy;
        private Instant reviewedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleSummaryDto {
        private String id;
        private String slug;
        private String title;
        private String excerpt;
        private String featuredImageUrl;
        private Integer readTimeMinutes;
    }
}
