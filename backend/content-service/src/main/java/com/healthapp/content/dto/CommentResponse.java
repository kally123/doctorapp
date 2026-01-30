package com.healthapp.content.dto;

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
public class CommentResponse {

    private String id;
    private String articleId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String parentCommentId;
    private Integer likeCount;
    private Boolean isEdited;
    private Instant createdAt;
    private List<CommentResponse> replies;
}
