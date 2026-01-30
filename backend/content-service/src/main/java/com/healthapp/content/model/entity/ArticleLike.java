package com.healthapp.content.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "article_likes")
@CompoundIndex(def = "{'articleId': 1, 'userId': 1}", unique = true)
public class ArticleLike {

    @Id
    private String id;

    @Indexed
    private String articleId;

    @Indexed
    private String userId;

    @CreatedDate
    private Instant createdAt;
}
