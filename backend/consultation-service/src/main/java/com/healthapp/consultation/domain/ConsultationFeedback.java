package com.healthapp.consultation.domain;

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

/**
 * Consultation feedback from patient after session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("consultation_feedback")
public class ConsultationFeedback {
    
    @Id
    private UUID id;
    
    @Column("session_id")
    private UUID sessionId;
    
    @Column("patient_id")
    private UUID patientId;
    
    @Column("doctor_id")
    private UUID doctorId;
    
    // Ratings (1-5 scale)
    @Column("overall_rating")
    private Integer overallRating;
    
    @Column("video_quality_rating")
    private Integer videoQualityRating;
    
    @Column("audio_quality_rating")
    private Integer audioQualityRating;
    
    @Column("doctor_rating")
    private Integer doctorRating;
    
    // Written feedback
    @Column("review_text")
    private String reviewText;
    
    // Would recommend
    @Column("would_recommend")
    private Boolean wouldRecommend;
    
    // Technical issues reported
    @Column("had_technical_issues")
    private Boolean hadTechnicalIssues;
    
    @Column("technical_issue_description")
    private String technicalIssueDescription;
    
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
}
