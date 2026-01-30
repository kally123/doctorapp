package com.healthapp.content.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "article_comments")
public class ArticleComment {

    @Id
    private String id;

    @Indexed
    private String articleId;

    @Indexed
    private String userId;
    private String userName;
    private String userAvatar;

    private String content;

    // For nested replies
    private String parentCommentId;
    private List<String> replyIds;

    private Integer likeCount;
    private Boolean isApproved;
    private Boolean isEdited;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
