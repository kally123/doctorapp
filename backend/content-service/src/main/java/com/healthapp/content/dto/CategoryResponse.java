package com.healthapp.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private String id;
    private String categoryId;
    private String name;
    private String description;
    private String icon;
    private String color;
    private String slug;
    private String parentCategoryId;
    private Long articleCount;
    private Integer order;
}
