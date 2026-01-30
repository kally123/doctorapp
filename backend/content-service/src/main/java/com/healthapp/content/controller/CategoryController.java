package com.healthapp.content.controller;

import com.healthapp.content.dto.CategoryResponse;
import com.healthapp.content.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Article category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public Flux<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories")
    public Flux<CategoryResponse> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get subcategories")
    public Flux<CategoryResponse> getSubcategories(@PathVariable String parentId) {
        return categoryService.getSubcategories(parentId);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public Mono<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug);
    }
}
