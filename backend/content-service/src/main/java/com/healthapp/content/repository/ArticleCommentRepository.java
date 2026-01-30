package com.healthapp.content.repository;

import com.healthapp.content.model.entity.ArticleComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ArticleCommentRepository extends ReactiveMongoRepository<ArticleComment, String> {

    Flux<ArticleComment> findByArticleIdAndParentCommentIdIsNullAndIsApprovedTrueOrderByCreatedAtDesc(
            String articleId, Pageable pageable);

    Flux<ArticleComment> findByParentCommentIdAndIsApprovedTrueOrderByCreatedAtAsc(String parentCommentId);

    Mono<Long> countByArticleIdAndIsApprovedTrue(String articleId);

    Flux<ArticleComment> findByIsApprovedFalseOrderByCreatedAtAsc(Pageable pageable);
}
