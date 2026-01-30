package com.healthapp.content.dto;

import com.healthapp.content.model.enums.ArticleStatus;
import com.healthapp.content.model.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFilter {

    private String query;
    private String categoryId;
    private List<String> tags;
    private Difficulty difficulty;
    private ArticleStatus status;
    private Boolean isFeatured;
    private Boolean isEditorsPick;
    private String authorId;
    private String sortBy; // "recent", "popular", "trending"
}
