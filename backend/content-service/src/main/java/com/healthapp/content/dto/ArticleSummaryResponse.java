package com.healthapp.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryResponse {

    private String id;
    private String slug;
    private String title;
    private String excerpt;
    private String featuredImageUrl;
    private String categoryName;
    private String categorySlug;
    private String authorName;
    private String authorAvatar;
    private Integer readTimeMinutes;
    private Long views;
    private Long likes;
    private Boolean isFeatured;
    private Boolean isEditorsPick;
    private String publishedAt;
}
