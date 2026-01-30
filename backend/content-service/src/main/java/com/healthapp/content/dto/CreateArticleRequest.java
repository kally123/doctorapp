package com.healthapp.content.dto;

import com.healthapp.content.model.enums.ArticleStatus;
import com.healthapp.content.model.enums.AuthorType;
import com.healthapp.content.model.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreateArticleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 300, message = "Subtitle must be at most 300 characters")
    private String subtitle;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Excerpt must be at most 500 characters")
    private String excerpt;

    private FeaturedImageDto featuredImage;

    private AuthorDto author;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String subcategoryId;

    private List<String> tags;

    private SeoDto seo;

    private ArticleStatus status;
    private Instant scheduledPublishAt;

    private Difficulty difficulty;

    private Boolean isFeatured;
    private Boolean isPremium;

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
        private AuthorType type;
        private String doctorId;
        private String name;
        private String specialization;
        private String credentials;
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
}
