package com.healthapp.content.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "article_categories")
public class ArticleCategory {

    @Id
    private String id;

    @Indexed(unique = true)
    private String categoryId;

    private String name;
    private String description;
    private String icon;
    private String color;

    @Indexed(unique = true)
    private String slug;

    private String parentCategoryId;
    private Long articleCount;
    private Integer order;
    private Boolean isActive;
}
