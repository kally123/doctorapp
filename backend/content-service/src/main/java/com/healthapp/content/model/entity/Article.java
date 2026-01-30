package com.healthapp.content.model.entity;

import com.healthapp.content.model.enums.ArticleStatus;
import com.healthapp.content.model.enums.AuthorType;
import com.healthapp.content.model.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "articles")
public class Article {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    // Content
    @TextIndexed(weight = 3)
    private String title;

    private String subtitle;

    @TextIndexed
    private String content;

    private String excerpt;

    // Media
    private FeaturedImage featuredImage;
    private List<Image> images;

    // Author
    private Author author;

    // Categorization
    @Indexed
    private Category category;
    private Category subcategory;

    @TextIndexed(weight = 2)
    @Indexed
    private List<String> tags;

    // SEO
    private Seo seo;

    // Publishing
    @Indexed
    private ArticleStatus status;
    private Instant publishedAt;
    private Instant scheduledPublishAt;

    // Reading
    private Integer readTimeMinutes;
    private Difficulty difficulty;

    // Engagement stats
    private ArticleStats stats;

    // Related content
    private List<String> relatedArticleIds;
    private List<String> relatedDoctorIds;
    private List<String> relatedTestIds;

    // Content flags
    private Boolean isFeatured;
    private Boolean isEditorsPick;
    private Boolean isPremium;

    // Medical review
    private MedicalReview medicalReview;

    // Audit
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private Integer version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturedImage {
        private String url;
        private String alt;
        private String caption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String url;
        private String alt;
        private String caption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
        private AuthorType type;
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
    public static class Category {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Seo {
        private String metaTitle;
        private String metaDescription;
        private List<String> keywords;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleStats {
        private Long views;
        private Long uniqueViews;
        private Long likes;
        private Long shares;
        private Long bookmarks;
        private Long comments;
        private Double avgReadTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalReview {
        private String reviewedBy;
        private Instant reviewedAt;
        private Instant nextReviewDate;
    }
}
