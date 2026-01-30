package com.healthapp.review.model.entity;

import com.healthapp.review.model.enums.ReportReason;
import com.healthapp.review.model.enums.ReportStatus;
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
@Table("review_reports")
public class ReviewReport {

    @Id
    private UUID id;

    @Column("review_id")
    private UUID reviewId;

    @Column("reporter_id")
    private UUID reporterId;

    private ReportReason reason;

    private String description;

    private ReportStatus status;

    @Column("reviewed_by")
    private UUID reviewedBy;

    @Column("reviewed_at")
    private Instant reviewedAt;

    @Column("action_taken")
    private String actionTaken;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
