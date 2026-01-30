package com.healthapp.review.model.entity;

import com.healthapp.review.model.enums.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("review_votes")
public class ReviewVote {

    @Id
    private UUID id;

    @Column("review_id")
    private UUID reviewId;

    @Column("user_id")
    private UUID userId;

    @Column("vote_type")
    private VoteType voteType;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
