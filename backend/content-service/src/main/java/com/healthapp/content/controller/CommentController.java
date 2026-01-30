package com.healthapp.content.controller;

import com.healthapp.content.dto.CommentResponse;
import com.healthapp.content.dto.CreateCommentRequest;
import com.healthapp.content.service.CommentService;
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
@RequestMapping("/api/v1/articles/{articleId}/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "Article comment management APIs")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to an article")
    public Mono<CommentResponse> addComment(
            @PathVariable String articleId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName,
            @RequestHeader(value = "X-User-Avatar", required = false) String userAvatar,
            @Valid @RequestBody CreateCommentRequest request) {
        
        return commentService.addComment(articleId, userId, userName, userAvatar, request);
    }

    @GetMapping
    @Operation(summary = "Get comments for an article")
    public Flux<CommentResponse> getArticleComments(
            @PathVariable String articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return commentService.getArticleComments(articleId, page, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a comment")
    public Mono<Void> deleteComment(
            @PathVariable String articleId,
            @PathVariable String commentId,
            @RequestHeader("X-User-Id") String userId) {
        
        return commentService.deleteComment(commentId, userId);
    }
}
