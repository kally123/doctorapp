package com.healthapp.content.service;

import com.healthapp.content.dto.CommentResponse;
import com.healthapp.content.dto.CreateCommentRequest;
import com.healthapp.content.model.entity.ArticleComment;
import com.healthapp.content.repository.ArticleCommentRepository;
import com.healthapp.content.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final ArticleCommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    /**
     * Add a comment to an article
     */
    @Transactional
    public Mono<CommentResponse> addComment(String articleId, String userId, 
                                            String userName, String userAvatar,
                                            CreateCommentRequest request) {
        ArticleComment comment = ArticleComment.builder()
                .articleId(articleId)
                .userId(userId)
                .userName(userName)
                .userAvatar(userAvatar)
                .content(sanitizeContent(request.getContent()))
                .parentCommentId(request.getParentCommentId())
                .likeCount(0)
                .isApproved(true) // Auto-approve for now
                .isEdited(false)
                .build();

        return commentRepository.save(comment)
                .flatMap(saved -> updateCommentCount(articleId).thenReturn(saved))
                .map(this::toResponse);
    }

    /**
     * Get comments for an article
     */
    public Flux<CommentResponse> getArticleComments(String articleId, int page, int size) {
        return commentRepository.findByArticleIdAndParentCommentIdIsNullAndIsApprovedTrueOrderByCreatedAtDesc(
                        articleId, PageRequest.of(page, size))
                .flatMap(this::enrichWithReplies);
    }

    /**
     * Delete a comment
     */
    @Transactional
    public Mono<Void> deleteComment(String commentId, String userId) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getUserId().equals(userId))
                .flatMap(comment -> commentRepository.delete(comment)
                        .then(updateCommentCount(comment.getArticleId())));
    }

    private Mono<CommentResponse> enrichWithReplies(ArticleComment comment) {
        return commentRepository.findByParentCommentIdAndIsApprovedTrueOrderByCreatedAtAsc(comment.getId())
                .map(this::toResponse)
                .collectList()
                .map(replies -> {
                    CommentResponse response = toResponse(comment);
                    response.setReplies(replies);
                    return response;
                });
    }

    private Mono<Void> updateCommentCount(String articleId) {
        return commentRepository.countByArticleIdAndIsApprovedTrue(articleId)
                .flatMap(count -> articleRepository.findById(articleId)
                        .flatMap(article -> {
                            if (article.getStats() != null) {
                                article.getStats().setComments(count);
                            }
                            return articleRepository.save(article);
                        }))
                .then();
    }

    private String sanitizeContent(String content) {
        return content.replaceAll("<[^>]*>", "").trim();
    }

    private CommentResponse toResponse(ArticleComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .userName(comment.getUserName())
                .userAvatar(comment.getUserAvatar())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .likeCount(comment.getLikeCount())
                .isEdited(comment.getIsEdited())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
