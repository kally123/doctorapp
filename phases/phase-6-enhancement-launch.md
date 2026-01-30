# Phase 6: Enhancement & Scale - Detailed Implementation Plan

## Phase Overview

| Attribute | Details |
|-----------|---------|
| **Duration** | 4 Weeks |
| **Start Date** | _Phase 5 End Date + 1 day_ |
| **End Date** | _Start Date + 4 weeks_ |
| **Team Size** | 16-20 members |
| **Goal** | Reviews & ratings, content platform, mobile apps, production readiness, and platform launch |

---

## Phase 6 Objectives

1. ✅ Implement doctor reviews and ratings system
2. ✅ Build health articles/content platform
3. ✅ Develop Patient mobile app (iOS & Android)
4. ✅ Develop Doctor mobile app (iOS & Android)
5. ✅ Set up production infrastructure with HA
6. ✅ Complete security audit and penetration testing
7. ✅ Achieve HIPAA compliance certification
8. ✅ Complete end-to-end and performance testing
9. ✅ Execute UAT with beta users
10. ✅ **LAUNCH PLATFORM** 🚀

---

## Prerequisites from Phase 5

Before starting Phase 6, ensure the following are complete:

| Prerequisite | Status |
|--------------|--------|
| Order Service deployed and functional | ⬜ |
| Lab Test booking operational | ⬜ |
| All Phase 1-5 features stable | ⬜ |
| Consultation Service video working | ⬜ |
| Prescription and EHR Services operational | ⬜ |
| Payment processing working | ⬜ |
| Notification Service multi-channel working | ⬜ |

---

## Team Allocation for Phase 6

| Role | Name | Focus Area |
|------|------|------------|
| Tech Lead | _TBD_ | Architecture, security review, launch coordination |
| Backend 1 | _TBD_ | Reviews & Ratings Service |
| Backend 2 | _TBD_ | Content Service |
| Backend 3 | _TBD_ | Performance optimization, bug fixes |
| Mobile 1 | _TBD_ | Patient Mobile App (React Native/Flutter) |
| Mobile 2 | _TBD_ | Patient Mobile App support |
| Mobile 3 | _TBD_ | Doctor Mobile App (React Native/Flutter) |
| Mobile 4 | _TBD_ | Doctor Mobile App support |
| Frontend 1 | _TBD_ | Reviews UI, Content pages |
| Frontend 2 | _TBD_ | Bug fixes, polish |
| DevOps 1 | _TBD_ | Production infrastructure |
| DevOps 2 | _TBD_ | Monitoring, scaling, DR |
| Security | _TBD_ | Security audit, penetration testing |
| QA Lead | _TBD_ | E2E testing, UAT coordination |
| QA 1 | _TBD_ | Web app testing |
| QA 2 | _TBD_ | Mobile app testing |

---

## Architecture Overview

### Phase 6 Additions to Platform

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         COMPLETE PLATFORM ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │  Patient    │ │   Doctor    │ │  Patient    │ │   Doctor    │           │
│  │  Web App    │ │  Dashboard  │ │ Mobile App  │ │ Mobile App  │           │
│  │  (React)    │ │   (React)   │ │ (RN/Flutter)│ │(RN/Flutter) │           │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘           │
│         │               │               │               │                   │
│         └───────────────┴───────────────┴───────────────┘                   │
│                                   │                                          │
│                          ┌────────▼────────┐                                │
│                          │   API Gateway   │                                │
│                          │   (Kong/AWS)    │                                │
│                          └────────┬────────┘                                │
│                                   │                                          │
│  ┌────────────────────────────────┼────────────────────────────────────┐   │
│  │                       MICROSERVICES LAYER                            │   │
│  │                                                                      │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │   │
│  │  │  User   │ │ Doctor  │ │Appoint- │ │ Consult │ │Prescrip-│       │   │
│  │  │ Service │ │ Service │ │  ment   │ │ ation   │ │  tion   │       │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘       │   │
│  │                                                                      │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐       │   │
│  │  │  EHR    │ │  Order  │ │ Payment │ │Notifica-│ │ Search  │       │   │
│  │  │ Service │ │ Service │ │ Service │ │  tion   │ │ Service │       │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘       │   │
│  │                                                                      │   │
│  │  ┌─────────────────────────────────────────────────────────────┐   │   │
│  │  │                   NEW IN PHASE 6                             │   │   │
│  │  │  ┌─────────────┐  ┌─────────────┐                           │   │   │
│  │  │  │   Review    │  │   Content   │                           │   │   │
│  │  │  │   Service   │  │   Service   │                           │   │   │
│  │  │  │  (Ratings)  │  │  (Articles) │                           │   │   │
│  │  │  └─────────────┘  └─────────────┘                           │   │   │
│  │  └─────────────────────────────────────────────────────────────┘   │   │
│  │                                                                      │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                   │                                          │
│  ┌────────────────────────────────┼────────────────────────────────────┐   │
│  │                      DATA LAYER                                      │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │   │
│  │  │PostgreSQL│ │ MongoDB  │ │  Redis   │ │Elastic-  │ │   S3     │  │   │
│  │  │  (HA)    │ │  (HA)    │ │ Cluster  │ │ search   │ │ Storage  │  │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                   PRODUCTION INFRASTRUCTURE                           │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │  │
│  │  │Kubernetes│ │  CDN     │ │ WAF      │ │Prometheus│ │  ELK     │   │  │
│  │  │  (HA)    │ │CloudFront│ │ Shield   │ │ Grafana  │ │  Stack   │   │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Mobile App Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MOBILE APP ARCHITECTURE                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      React Native / Flutter App                        │ │
│  │                                                                        │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │ │
│  │  │   Screens   │  │  Components │  │   Hooks/    │  │   State     │  │ │
│  │  │             │  │             │  │  Providers  │  │ Management  │  │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  │ │
│  │                                                                        │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                      Core Services Layer                         │  │ │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │  │ │
│  │  │  │   API    │ │  Auth    │ │   Push   │ │  Video   │           │  │ │
│  │  │  │  Client  │ │ Service  │ │ Notif.   │ │   SDK    │           │  │ │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  │                                                                        │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │                      Native Modules                              │  │ │
│  │  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │  │ │
│  │  │  │ Camera   │ │ Biometric│ │ Storage  │ │ Deep     │           │  │ │
│  │  │  │ Access   │ │   Auth   │ │(Keychain)│ │ Linking  │           │  │ │
│  │  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│                    iOS                              Android                  │
│  ┌───────────────────────────────┐  ┌───────────────────────────────────┐  │
│  │  • Swift/Kotlin Bridges       │  │  • Kotlin/Java Bridges            │  │
│  │  • Apple Push (APNs)          │  │  • Firebase Cloud Messaging       │  │
│  │  • HealthKit Integration      │  │  • Google Fit Integration         │  │
│  │  • Face ID / Touch ID         │  │  • Fingerprint / Face Unlock      │  │
│  │  • App Store Distribution     │  │  • Play Store Distribution        │  │
│  └───────────────────────────────┘  └───────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Sprint Breakdown

### Sprint 12 (Week 23-24): Reviews, Ratings & Content

**Sprint Goal**: Patients can review doctors. Health articles content platform live.

---

#### Backend Tasks - Reviews & Ratings

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B17.1 | Reviews Database Schema | Design reviews and ratings tables | Backend 1 | 8 | P0 | Schema complete |
| B17.2 | Submit Review API | Create review with rating | Backend 1 | 12 | P0 | Reviews created |
| B17.3 | Rating Aggregation | Calculate and cache doctor ratings | Backend 1 | 12 | P0 | Ratings calculated |
| B17.4 | Sync to Search | Update doctor search with ratings | Backend 1 | 8 | P0 | Search updated |
| B17.5 | Review Moderation | Flag/approve/reject reviews | Backend 1 | 12 | P1 | Moderation works |
| B17.6 | Doctor Response API | Doctor can respond to reviews | Backend 1 | 8 | P1 | Responses work |
| B17.7 | Helpful Votes | Upvote helpful reviews | Backend 1 | 6 | P2 | Voting works |

**Database Schema:**

<details>
<summary><strong>B17.1 - Reviews Database Schema</strong></summary>

```sql
-- V1__create_reviews_tables.sql

-- Review status
CREATE TYPE review_status AS ENUM (
    'PENDING',      -- Awaiting moderation
    'APPROVED',     -- Published
    'REJECTED',     -- Rejected by moderation
    'FLAGGED',      -- Flagged for review
    'HIDDEN'        -- Hidden by admin
);

-- Main reviews table
CREATE TABLE doctor_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- References
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    consultation_id UUID,           -- Optional link to consultation
    appointment_id UUID,            -- Optional link to appointment
    
    -- Ratings (1-5 scale)
    overall_rating INT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    
    -- Detailed ratings (optional)
    wait_time_rating INT CHECK (wait_time_rating BETWEEN 1 AND 5),
    bedside_manner_rating INT CHECK (bedside_manner_rating BETWEEN 1 AND 5),
    explanation_rating INT CHECK (explanation_rating BETWEEN 1 AND 5),
    
    -- Review content
    title VARCHAR(200),
    review_text TEXT,
    
    -- Consultation type reviewed
    consultation_type VARCHAR(20),  -- 'IN_PERSON', 'VIDEO', 'AUDIO'
    
    -- Tags
    positive_tags TEXT[],           -- ['professional', 'punctual', 'thorough']
    improvement_tags TEXT[],        -- ['wait_time', 'availability']
    
    -- Verification
    is_verified BOOLEAN DEFAULT TRUE,   -- Verified consultation
    
    -- Moderation
    status review_status DEFAULT 'PENDING',
    moderation_notes TEXT,
    moderated_by UUID,
    moderated_at TIMESTAMP WITH TIME ZONE,
    
    -- Doctor response
    doctor_response TEXT,
    doctor_responded_at TIMESTAMP WITH TIME ZONE,
    
    -- Engagement
    helpful_count INT DEFAULT 0,
    not_helpful_count INT DEFAULT 0,
    report_count INT DEFAULT 0,
    
    -- Visibility
    is_anonymous BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Prevent duplicate reviews
    CONSTRAINT unique_patient_doctor_consultation 
        UNIQUE (patient_id, doctor_id, consultation_id)
);

-- Indexes
CREATE INDEX idx_reviews_doctor ON doctor_reviews(doctor_id);
CREATE INDEX idx_reviews_patient ON doctor_reviews(patient_id);
CREATE INDEX idx_reviews_status ON doctor_reviews(status);
CREATE INDEX idx_reviews_rating ON doctor_reviews(overall_rating);
CREATE INDEX idx_reviews_created ON doctor_reviews(created_at);

-- Doctor rating aggregates (cached)
CREATE TABLE doctor_rating_aggregates (
    doctor_id UUID PRIMARY KEY,
    
    -- Overall rating
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    total_reviews INT NOT NULL DEFAULT 0,
    
    -- Rating distribution
    five_star_count INT DEFAULT 0,
    four_star_count INT DEFAULT 0,
    three_star_count INT DEFAULT 0,
    two_star_count INT DEFAULT 0,
    one_star_count INT DEFAULT 0,
    
    -- Detailed averages
    avg_wait_time_rating DECIMAL(3, 2),
    avg_bedside_manner_rating DECIMAL(3, 2),
    avg_explanation_rating DECIMAL(3, 2),
    
    -- By consultation type
    video_consultation_rating DECIMAL(3, 2),
    video_consultation_count INT DEFAULT 0,
    in_person_rating DECIMAL(3, 2),
    in_person_count INT DEFAULT 0,
    
    -- Top tags
    top_positive_tags JSONB,        -- [{"tag": "professional", "count": 45}, ...]
    top_improvement_tags JSONB,
    
    -- Recommendation rate
    recommendation_rate DECIMAL(5, 2),  -- % who would recommend
    
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Review helpful votes
CREATE TABLE review_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL REFERENCES doctor_reviews(id),
    user_id UUID NOT NULL,
    vote_type VARCHAR(20) NOT NULL,  -- 'HELPFUL', 'NOT_HELPFUL'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_user_review_vote UNIQUE (review_id, user_id)
);

CREATE INDEX idx_votes_review ON review_votes(review_id);

-- Review reports (for moderation)
CREATE TABLE review_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL REFERENCES doctor_reviews(id),
    reporter_id UUID NOT NULL,
    
    reason VARCHAR(50) NOT NULL,    -- 'SPAM', 'FAKE', 'INAPPROPRIATE', 'OTHER'
    description TEXT,
    
    status VARCHAR(20) DEFAULT 'PENDING',  -- 'PENDING', 'REVIEWED', 'ACTIONED'
    reviewed_by UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    action_taken VARCHAR(100),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_reports_review ON review_reports(review_id);
CREATE INDEX idx_reports_status ON review_reports(status);

-- Trigger for rating aggregation
CREATE OR REPLACE FUNCTION update_doctor_rating_aggregate()
RETURNS TRIGGER AS $$
BEGIN
    -- Recalculate aggregates for the doctor
    INSERT INTO doctor_rating_aggregates (doctor_id, average_rating, total_reviews)
    SELECT 
        COALESCE(NEW.doctor_id, OLD.doctor_id),
        COALESCE(AVG(overall_rating), 0),
        COUNT(*)
    FROM doctor_reviews
    WHERE doctor_id = COALESCE(NEW.doctor_id, OLD.doctor_id)
      AND status = 'APPROVED'
    ON CONFLICT (doctor_id) 
    DO UPDATE SET
        average_rating = EXCLUDED.average_rating,
        total_reviews = EXCLUDED.total_reviews,
        five_star_count = (SELECT COUNT(*) FROM doctor_reviews 
                          WHERE doctor_id = EXCLUDED.doctor_id 
                          AND status = 'APPROVED' AND overall_rating = 5),
        four_star_count = (SELECT COUNT(*) FROM doctor_reviews 
                          WHERE doctor_id = EXCLUDED.doctor_id 
                          AND status = 'APPROVED' AND overall_rating = 4),
        three_star_count = (SELECT COUNT(*) FROM doctor_reviews 
                           WHERE doctor_id = EXCLUDED.doctor_id 
                           AND status = 'APPROVED' AND overall_rating = 3),
        two_star_count = (SELECT COUNT(*) FROM doctor_reviews 
                         WHERE doctor_id = EXCLUDED.doctor_id 
                         AND status = 'APPROVED' AND overall_rating = 2),
        one_star_count = (SELECT COUNT(*) FROM doctor_reviews 
                         WHERE doctor_id = EXCLUDED.doctor_id 
                         AND status = 'APPROVED' AND overall_rating = 1),
        last_updated = NOW();
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER review_aggregate_trigger
    AFTER INSERT OR UPDATE OR DELETE ON doctor_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_doctor_rating_aggregate();
```
</details>

<details>
<summary><strong>B17.2 - Submit Review API (Detailed)</strong></summary>

```java
// ReviewService.java
@Service
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final RatingAggregateRepository aggregateRepository;
    private final ConsultationClient consultationClient;
    private final DoctorSearchClient searchClient;
    
    /**
     * Submit a new review
     */
    public Mono<Review> submitReview(String patientId, SubmitReviewRequest request) {
        // Validate consultation exists and belongs to patient
        return consultationClient.getConsultation(request.getConsultationId())
            .filter(c -> c.getPatientId().equals(patientId))
            .switchIfEmpty(Mono.error(new UnauthorizedException()))
            .filter(c -> c.getStatus().equals("COMPLETED"))
            .switchIfEmpty(Mono.error(new InvalidOperationException(
                "Can only review completed consultations")))
            .flatMap(consultation -> {
                // Check for existing review
                return reviewRepository.existsByPatientAndConsultation(
                    patientId, request.getConsultationId())
                    .filter(exists -> !exists)
                    .switchIfEmpty(Mono.error(new DuplicateReviewException()))
                    .then(createReview(patientId, consultation, request));
            });
    }
    
    private Mono<Review> createReview(
        String patientId,
        Consultation consultation,
        SubmitReviewRequest request
    ) {
        Review review = Review.builder()
            .doctorId(consultation.getDoctorId())
            .patientId(patientId)
            .consultationId(consultation.getId())
            .appointmentId(consultation.getAppointmentId())
            .overallRating(request.getOverallRating())
            .waitTimeRating(request.getWaitTimeRating())
            .bedsideMannerRating(request.getBedsideMannerRating())
            .explanationRating(request.getExplanationRating())
            .title(request.getTitle())
            .reviewText(sanitizeText(request.getReviewText()))
            .consultationType(consultation.getType())
            .positiveTags(request.getPositiveTags())
            .improvementTags(request.getImprovementTags())
            .isVerified(true)
            .status(autoModerate(request) ? ReviewStatus.APPROVED : ReviewStatus.PENDING)
            .isAnonymous(request.isAnonymous())
            .build();
        
        return reviewRepository.save(review)
            .doOnSuccess(savedReview -> {
                // Async: Update aggregates
                updateDoctorRatings(savedReview.getDoctorId()).subscribe();
                
                // Async: Update search index
                syncToSearch(savedReview.getDoctorId()).subscribe();
                
                // Async: Notify doctor
                notifyDoctorOfReview(savedReview).subscribe();
            });
    }
    
    /**
     * Get reviews for a doctor
     */
    public Flux<ReviewDto> getDoctorReviews(
        String doctorId,
        ReviewFilter filter,
        Pageable pageable
    ) {
        return reviewRepository.findByDoctorWithFilter(doctorId, filter, pageable)
            .filter(r -> r.getStatus() == ReviewStatus.APPROVED)
            .map(this::toDto);
    }
    
    /**
     * Get rating aggregate for doctor
     */
    public Mono<DoctorRatingAggregate> getDoctorRating(String doctorId) {
        return aggregateRepository.findByDoctorId(doctorId)
            .defaultIfEmpty(DoctorRatingAggregate.empty(doctorId));
    }
    
    /**
     * Doctor responds to review
     */
    public Mono<Review> respondToReview(
        String doctorId,
        String reviewId,
        String response
    ) {
        return reviewRepository.findById(reviewId)
            .filter(r -> r.getDoctorId().equals(doctorId))
            .switchIfEmpty(Mono.error(new UnauthorizedException()))
            .filter(r -> r.getDoctorResponse() == null)
            .switchIfEmpty(Mono.error(new InvalidOperationException(
                "Already responded to this review")))
            .flatMap(review -> {
                review.setDoctorResponse(sanitizeText(response));
                review.setDoctorRespondedAt(Instant.now());
                return reviewRepository.save(review);
            })
            .doOnSuccess(review -> 
                notifyPatientOfResponse(review).subscribe()
            );
    }
    
    /**
     * Vote review as helpful
     */
    public Mono<Void> voteReview(String userId, String reviewId, VoteType voteType) {
        return voteRepository.findByReviewAndUser(reviewId, userId)
            .flatMap(existingVote -> {
                if (existingVote.getVoteType() == voteType) {
                    // Remove vote
                    return voteRepository.delete(existingVote)
                        .then(updateVoteCounts(reviewId));
                } else {
                    // Change vote
                    existingVote.setVoteType(voteType);
                    return voteRepository.save(existingVote)
                        .then(updateVoteCounts(reviewId));
                }
            })
            .switchIfEmpty(
                // New vote
                voteRepository.save(ReviewVote.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .voteType(voteType)
                    .build())
                .then(updateVoteCounts(reviewId))
            );
    }
    
    /**
     * Auto-moderation for reviews
     */
    private boolean autoModerate(SubmitReviewRequest request) {
        // Check for profanity, spam patterns, etc.
        if (containsProfanity(request.getReviewText())) {
            return false;
        }
        if (request.getReviewText() != null && request.getReviewText().length() < 10) {
            return false; // Too short, needs manual review
        }
        // Auto-approve most reviews
        return true;
    }
    
    /**
     * Sync ratings to search index
     */
    private Mono<Void> syncToSearch(String doctorId) {
        return aggregateRepository.findByDoctorId(doctorId)
            .flatMap(aggregate -> searchClient.updateDoctorRating(
                doctorId,
                aggregate.getAverageRating(),
                aggregate.getTotalReviews()
            ));
    }
}

@Value
@Builder
public class SubmitReviewRequest {
    @NotNull
    String consultationId;
    
    @Min(1) @Max(5)
    int overallRating;
    
    Integer waitTimeRating;
    Integer bedsideMannerRating;
    Integer explanationRating;
    
    @Size(max = 200)
    String title;
    
    @Size(max = 2000)
    String reviewText;
    
    List<String> positiveTags;
    List<String> improvementTags;
    
    boolean anonymous;
}
```
</details>

---

#### Backend Tasks - Content Service

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| B17.8 | Create Content Service | Spring Boot project setup | Backend 2 | 4 | P1 | Service builds |
| B17.9 | Articles Database Schema | MongoDB schema for articles | Backend 2 | 8 | P1 | Schema complete |
| B17.10 | Articles CRUD API | Create, read, update, delete | Backend 2 | 12 | P1 | CRUD works |
| B17.11 | Article Search | Elasticsearch indexing and search | Backend 2 | 12 | P1 | Search works |
| B17.12 | Article Categories | Category management | Backend 2 | 6 | P1 | Categories work |
| B17.13 | Related Articles | Recommendation engine | Backend 2 | 16 | P2 | Recommendations work |
| B17.14 | Article Analytics | Views, likes, shares tracking | Backend 2 | 8 | P2 | Analytics tracked |

<details>
<summary><strong>B17.9 - Articles MongoDB Schema</strong></summary>

```javascript
// MongoDB Collection: articles

{
    "_id": ObjectId,
    "articleId": "uuid-string",
    "slug": "10-tips-for-healthy-heart",  // URL-friendly slug
    
    // Content
    "title": "10 Tips for a Healthy Heart",
    "subtitle": "Simple lifestyle changes that can make a big difference",
    "content": "Full markdown/HTML content...",
    "excerpt": "Short summary for listings...",
    
    // Media
    "featuredImage": {
        "url": "https://cdn.healthapp.com/articles/heart-health.jpg",
        "alt": "Healthy heart illustration",
        "caption": "Image source: HealthApp"
    },
    "images": [...],
    
    // Author
    "author": {
        "type": "DOCTOR" | "EDITORIAL",
        "doctorId": "uuid-string",        // If written by doctor
        "name": "Dr. Sarah Smith",
        "avatar": "https://...",
        "specialization": "Cardiology",
        "credentials": "MD, FACC"
    },
    
    // Categorization
    "category": {
        "id": "cardiology",
        "name": "Heart Health"
    },
    "subcategory": {
        "id": "prevention",
        "name": "Prevention & Lifestyle"
    },
    "tags": ["heart", "lifestyle", "prevention", "diet", "exercise"],
    
    // SEO
    "seo": {
        "metaTitle": "10 Tips for a Healthy Heart | HealthApp",
        "metaDescription": "Learn simple lifestyle changes...",
        "keywords": ["heart health", "cardiovascular", "healthy living"]
    },
    
    // Publishing
    "status": "DRAFT" | "REVIEW" | "PUBLISHED" | "ARCHIVED",
    "publishedAt": ISODate,
    "scheduledPublishAt": ISODate,
    
    // Reading
    "readTimeMinutes": 5,
    "difficulty": "BEGINNER" | "INTERMEDIATE" | "ADVANCED",
    
    // Engagement
    "stats": {
        "views": 12500,
        "uniqueViews": 8900,
        "likes": 456,
        "shares": 123,
        "bookmarks": 234,
        "comments": 45,
        "avgReadTime": 4.2
    },
    
    // Related content
    "relatedArticles": ["article-id-1", "article-id-2"],
    "relatedDoctors": ["doctor-id-1"],
    "relatedTests": ["test-id-1"],       // Lab tests mentioned
    
    // Content flags
    "isFeatured": true,
    "isEditorsPick": false,
    "isPremium": false,
    
    // Medical review
    "medicalReview": {
        "reviewedBy": "Dr. John Doe",
        "reviewedAt": ISODate,
        "nextReviewDate": ISODate
    },
    
    // Audit
    "createdAt": ISODate,
    "updatedAt": ISODate,
    "createdBy": "user-id",
    "version": 3
}

// Indexes
db.articles.createIndex({ "slug": 1 }, { unique: true });
db.articles.createIndex({ "status": 1, "publishedAt": -1 });
db.articles.createIndex({ "category.id": 1 });
db.articles.createIndex({ "tags": 1 });
db.articles.createIndex({ "author.doctorId": 1 });
db.articles.createIndex({ 
    "title": "text", 
    "content": "text", 
    "tags": "text" 
});

// Article categories
{
    "_id": ObjectId,
    "categoryId": "cardiology",
    "name": "Heart Health",
    "description": "Articles about cardiovascular health",
    "icon": "heart",
    "color": "#e74c3c",
    "slug": "heart-health",
    "parentCategory": null,
    "articleCount": 45,
    "order": 1,
    "isActive": true
}
```
</details>

---

#### Frontend Tasks - Sprint 12

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| F12.1 | Review Submission Flow | Post-consultation review modal | Frontend 1 | 16 | P0 | Review submitted |
| F12.2 | Reviews on Doctor Profile | Display reviews with ratings | Frontend 1 | 12 | P0 | Reviews displayed |
| F12.3 | Doctor Response UI | Doctor can respond to reviews | Frontend 2 | 8 | P1 | Response works |
| F12.4 | Health Articles Listing | Article list with categories | Frontend 1 | 16 | P1 | List displays |
| F12.5 | Article Detail Page | Full article with related content | Frontend 1 | 12 | P1 | Article displays |
| F12.6 | Article Search | Search and filter articles | Frontend 1 | 8 | P1 | Search works |
| F12.7 | Review Moderation UI | Admin review moderation | Frontend 2 | 12 | P1 | Moderation works |

<details>
<summary><strong>F12.1 - Review Submission Flow (Detailed)</strong></summary>

```markdown
## Review Submission Modal

┌─────────────────────────────────────────────────────────────────────────┐
│  Rate Your Experience                                              [×] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  👨‍⚕️ Dr. Sarah Smith                                              │ │
│  │     Cardiologist | Video Consultation                              │ │
│  │     January 30, 2026                                               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  Overall Rating *                                                        │
│                                                                          │
│           ⭐ ⭐ ⭐ ⭐ ⭐                                                │
│              (Tap to rate)                                              │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Detailed Ratings (Optional)                                    [Show] │
│                                                                          │
│  Wait Time         ☆ ☆ ☆ ☆ ☆                                          │
│  Bedside Manner    ☆ ☆ ☆ ☆ ☆                                          │
│  Explanation       ☆ ☆ ☆ ☆ ☆                                          │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  What did you like? (Select all that apply)                             │
│                                                                          │
│  [✓ Professional] [✓ Punctual] [○ Thorough] [○ Friendly]              │
│  [○ Great listener] [○ Clear explanations] [○ Follow-up care]         │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Write a Review (Optional)                                              │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ Dr. Smith was very thorough in her examination and took the time  │ │
│  │ to explain my condition clearly. Highly recommend!                 │ │
│  │                                                                     │ │
│  │                                                                     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│  0/2000 characters                                                       │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  ☐ Post anonymously                                                     │
│                                                                          │
│  By submitting, you agree to our review guidelines.                     │
│                                                                          │
│                                    [Cancel]  [Submit Review]            │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

<details>
<summary><strong>F12.2 - Reviews on Doctor Profile (Detailed)</strong></summary>

```markdown
## Reviews Section on Doctor Profile

┌─────────────────────────────────────────────────────────────────────────┐
│  Reviews & Ratings                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────┐  ┌────────────────────────────────────────────┐ │
│  │                    │  │  Rating Distribution                       │ │
│  │      4.8           │  │  ⭐⭐⭐⭐⭐ █████████████████████░░ 85%   │ │
│  │     ⭐⭐⭐⭐⭐      │  │  ⭐⭐⭐⭐   ████░░░░░░░░░░░░░░░░░░ 10%   │ │
│  │   245 reviews      │  │  ⭐⭐⭐     ██░░░░░░░░░░░░░░░░░░░░░ 3%    │ │
│  │                    │  │  ⭐⭐       ░░░░░░░░░░░░░░░░░░░░░░░ 1%    │ │
│  │   98% recommend    │  │  ⭐         ░░░░░░░░░░░░░░░░░░░░░░░ 1%    │ │
│  └────────────────────┘  └────────────────────────────────────────────┘ │
│                                                                          │
│  Most mentioned: [Professional] [Punctual] [Clear explanations]         │
│                                                                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Filter: [All ▼]  [Most Recent ▼]  [Video ▼]                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  ⭐⭐⭐⭐⭐  "Excellent Doctor!"                                   │ │
│  │  John D. | Video Consultation | Jan 28, 2026         ✓ Verified   │ │
│  │                                                                     │ │
│  │  Dr. Smith was incredibly thorough and patient. She explained      │ │
│  │  everything clearly and answered all my questions. The video       │ │
│  │  consultation was smooth and professional.                         │ │
│  │                                                                     │ │
│  │  [👍 Helpful (12)]  [👎]  [🚩 Report]                              │ │
│  │                                                                     │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │  💬 Doctor's Response                           Jan 29, 2026 │ │ │
│  │  │  Thank you for your kind words, John! It was a pleasure      │ │ │
│  │  │  helping you. Don't forget your follow-up in 2 weeks.        │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  ⭐⭐⭐⭐⭐  "Highly Recommend!"                                   │ │
│  │  Anonymous | In-Person | Jan 25, 2026                ✓ Verified   │ │
│  │                                                                     │ │
│  │  Best cardiologist I've seen. Very professional and knowledgeable.│ │
│  │  Minimal wait time which was a pleasant surprise.                  │ │
│  │                                                                     │ │
│  │  [👍 Helpful (8)]  [👎]  [🚩 Report]                               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  [Load More Reviews...]                                                  │
└─────────────────────────────────────────────────────────────────────────┘
```
</details>

---

### Sprint 13 (Week 25): Mobile App Development

**Sprint Goal**: Patient and Doctor mobile apps MVP ready for both platforms.

---

#### Mobile Tasks - Patient App

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| M1.1 | Project Setup | React Native/Flutter project structure | Mobile 1 | 8 | P0 | Project builds |
| M1.2 | Authentication | Login, register, OTP, biometric | Mobile 1 | 20 | P0 | Auth works |
| M1.3 | Doctor Search | Search with filters, map view | Mobile 1 | 20 | P0 | Search works |
| M1.4 | Doctor Profile | View doctor details, reviews | Mobile 1 | 12 | P0 | Profile displays |
| M1.5 | Booking Flow | Select slot, payment, confirmation | Mobile 1, 2 | 28 | P0 | Booking works |
| M1.6 | Video Consultation | Video call integration | Mobile 1, 2 | 36 | P0 | Video works |
| M1.7 | Prescriptions | View prescriptions, download PDF | Mobile 2 | 16 | P0 | Prescriptions display |
| M1.8 | Health Records | View EHR, upload documents | Mobile 2 | 16 | P1 | Records accessible |
| M1.9 | Push Notifications | FCM/APNs integration | Mobile 1 | 12 | P0 | Notifications received |
| M1.10 | Pharmacy Orders | Order medicines from Rx | Mobile 2 | 20 | P1 | Ordering works |
| M1.11 | Profile & Settings | User profile, preferences | Mobile 2 | 8 | P0 | Profile works |

<details>
<summary><strong>Mobile App Project Structure (React Native)</strong></summary>

```
patient-mobile-app/
├── android/                          # Android native code
├── ios/                              # iOS native code
├── src/
│   ├── App.tsx                       # Root component
│   ├── navigation/
│   │   ├── AppNavigator.tsx          # Main navigation
│   │   ├── AuthNavigator.tsx         # Auth stack
│   │   ├── MainNavigator.tsx         # Tab navigator
│   │   └── types.ts                  # Navigation types
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   ├── RegisterScreen.tsx
│   │   │   ├── OtpScreen.tsx
│   │   │   └── ForgotPasswordScreen.tsx
│   │   ├── home/
│   │   │   ├── HomeScreen.tsx
│   │   │   └── SearchScreen.tsx
│   │   ├── doctors/
│   │   │   ├── DoctorListScreen.tsx
│   │   │   ├── DoctorProfileScreen.tsx
│   │   │   ├── BookingScreen.tsx
│   │   │   └── BookingConfirmScreen.tsx
│   │   ├── consultation/
│   │   │   ├── WaitingRoomScreen.tsx
│   │   │   ├── VideoCallScreen.tsx
│   │   │   └── ConsultationSummaryScreen.tsx
│   │   ├── prescriptions/
│   │   │   ├── PrescriptionListScreen.tsx
│   │   │   └── PrescriptionDetailScreen.tsx
│   │   ├── records/
│   │   │   ├── HealthRecordsScreen.tsx
│   │   │   └── RecordDetailScreen.tsx
│   │   ├── pharmacy/
│   │   │   ├── PharmacyScreen.tsx
│   │   │   ├── CartScreen.tsx
│   │   │   └── OrderTrackingScreen.tsx
│   │   └── profile/
│   │       ├── ProfileScreen.tsx
│   │       └── SettingsScreen.tsx
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Avatar.tsx
│   │   │   └── LoadingSpinner.tsx
│   │   ├── doctors/
│   │   │   ├── DoctorCard.tsx
│   │   │   ├── DoctorRating.tsx
│   │   │   └── SlotPicker.tsx
│   │   ├── consultation/
│   │   │   ├── VideoControls.tsx
│   │   │   ├── ChatPanel.tsx
│   │   │   └── ParticipantView.tsx
│   │   └── ...
│   ├── services/
│   │   ├── api/
│   │   │   ├── client.ts             # Axios/fetch setup
│   │   │   ├── auth.ts
│   │   │   ├── doctors.ts
│   │   │   ├── appointments.ts
│   │   │   ├── consultations.ts
│   │   │   └── prescriptions.ts
│   │   ├── auth/
│   │   │   ├── AuthContext.tsx
│   │   │   └── authService.ts
│   │   ├── notifications/
│   │   │   └── pushService.ts
│   │   └── video/
│   │       └── twilioService.ts
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useDoctorSearch.ts
│   │   ├── useBooking.ts
│   │   └── useVideoCall.ts
│   ├── store/
│   │   ├── index.ts                  # Redux/Zustand setup
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── doctorSlice.ts
│   │   │   └── cartSlice.ts
│   │   └── selectors/
│   ├── utils/
│   │   ├── constants.ts
│   │   ├── helpers.ts
│   │   └── validation.ts
│   ├── theme/
│   │   ├── colors.ts
│   │   ├── typography.ts
│   │   └── spacing.ts
│   └── types/
│       └── index.ts
├── __tests__/
├── package.json
├── tsconfig.json
├── babel.config.js
├── metro.config.js
└── app.json
```
</details>

<details>
<summary><strong>M1.6 - Video Consultation (Mobile)</strong></summary>

```typescript
// screens/consultation/VideoCallScreen.tsx

import React, { useEffect, useState } from 'react';
import { View, StyleSheet, Alert } from 'react-native';
import { 
    TwilioVideo, 
    TwilioVideoLocalView,
    TwilioVideoParticipantView 
} from 'react-native-twilio-video-webrtc';
import { useVideoCall } from '../../hooks/useVideoCall';
import { VideoControls } from '../../components/consultation/VideoControls';
import { ChatPanel } from '../../components/consultation/ChatPanel';

interface VideoCallScreenProps {
    route: {
        params: {
            sessionId: string;
            accessToken: string;
            roomName: string;
        };
    };
}

export const VideoCallScreen: React.FC<VideoCallScreenProps> = ({ route }) => {
    const { sessionId, accessToken, roomName } = route.params;
    
    const {
        isConnected,
        isAudioEnabled,
        isVideoEnabled,
        remoteParticipant,
        callDuration,
        connectionQuality,
        connect,
        disconnect,
        toggleAudio,
        toggleVideo,
        switchCamera,
    } = useVideoCall(accessToken, roomName);
    
    const [isChatVisible, setIsChatVisible] = useState(false);
    
    useEffect(() => {
        connect();
        return () => disconnect();
    }, []);
    
    const handleEndCall = () => {
        Alert.alert(
            'End Consultation',
            'Are you sure you want to end this consultation?',
            [
                { text: 'Cancel', style: 'cancel' },
                { 
                    text: 'End Call', 
                    style: 'destructive',
                    onPress: () => disconnect()
                }
            ]
        );
    };
    
    return (
        <View style={styles.container}>
            {/* Remote participant (full screen) */}
            {remoteParticipant ? (
                <TwilioVideoParticipantView
                    style={styles.remoteVideo}
                    participantSid={remoteParticipant.sid}
                    trackSid={remoteParticipant.videoTrackSid}
                />
            ) : (
                <View style={styles.waitingContainer}>
                    <Text style={styles.waitingText}>
                        Waiting for doctor to join...
                    </Text>
                </View>
            )}
            
            {/* Local preview (picture-in-picture) */}
            <View style={styles.localVideoContainer}>
                <TwilioVideoLocalView
                    style={styles.localVideo}
                    enabled={isVideoEnabled}
                />
            </View>
            
            {/* Connection quality indicator */}
            <ConnectionQuality quality={connectionQuality} />
            
            {/* Call duration */}
            <CallTimer duration={callDuration} />
            
            {/* Chat panel (slide in from right) */}
            <ChatPanel
                visible={isChatVisible}
                sessionId={sessionId}
                onClose={() => setIsChatVisible(false)}
            />
            
            {/* Video controls */}
            <VideoControls
                isAudioEnabled={isAudioEnabled}
                isVideoEnabled={isVideoEnabled}
                onToggleAudio={toggleAudio}
                onToggleVideo={toggleVideo}
                onSwitchCamera={switchCamera}
                onOpenChat={() => setIsChatVisible(true)}
                onEndCall={handleEndCall}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000',
    },
    remoteVideo: {
        flex: 1,
    },
    localVideoContainer: {
        position: 'absolute',
        top: 60,
        right: 16,
        width: 120,
        height: 160,
        borderRadius: 12,
        overflow: 'hidden',
        borderWidth: 2,
        borderColor: '#fff',
    },
    localVideo: {
        flex: 1,
    },
    waitingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    waitingText: {
        color: '#fff',
        fontSize: 18,
    },
});
```
</details>

---

#### Mobile Tasks - Doctor App

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| M2.1 | Project Setup | React Native/Flutter project | Mobile 3 | 8 | P0 | Project builds |
| M2.2 | Authentication | Doctor login, profile verification | Mobile 3 | 16 | P0 | Auth works |
| M2.3 | Dashboard | Today's appointments, stats | Mobile 3 | 16 | P0 | Dashboard displays |
| M2.4 | Appointments List | View all appointments, filters | Mobile 3 | 12 | P0 | List works |
| M2.5 | Video Consultation | Video call from doctor side | Mobile 3, 4 | 36 | P0 | Video works |
| M2.6 | Prescription Writing | Create prescriptions on mobile | Mobile 4 | 28 | P0 | Prescriptions created |
| M2.7 | Patient History | View patient's EHR | Mobile 4 | 12 | P1 | Records accessible |
| M2.8 | Availability Management | Set/update availability | Mobile 4 | 12 | P1 | Availability works |
| M2.9 | Earnings & Payouts | View earnings, payout history | Mobile 4 | 8 | P1 | Earnings display |
| M2.10 | Push Notifications | FCM/APNs integration | Mobile 3 | 12 | P0 | Notifications received |

---

### Sprint 14 (Week 26): Production & Launch

**Sprint Goal**: Production environment ready. Security audit passed. UAT complete. LAUNCH!

---

#### DevOps Tasks - Production Infrastructure

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| D2.1 | Production K8s Cluster | Multi-AZ Kubernetes setup | DevOps 1 | 20 | P0 | Cluster operational |
| D2.2 | Production Databases | HA PostgreSQL, MongoDB, Redis | DevOps 1 | 20 | P0 | Databases operational |
| D2.3 | CDN Configuration | CloudFront for static assets | DevOps 2 | 8 | P0 | CDN serving assets |
| D2.4 | Auto-scaling Policies | HPA for all services | DevOps 2 | 12 | P0 | Scaling works |
| D2.5 | Disaster Recovery | DR setup, backup verification | DevOps 1 | 20 | P0 | DR tested |
| D2.6 | Production Monitoring | Prometheus, Grafana, alerts | DevOps 2 | 12 | P0 | Monitoring active |
| D2.7 | Load Testing | Performance testing at scale | DevOps 1, 2 | 20 | P0 | Tests pass targets |
| D2.8 | Runbooks | Operations documentation | DevOps 1 | 16 | P0 | Runbooks complete |
| D2.9 | SSL/TLS | Certificate management | DevOps 2 | 8 | P0 | HTTPS everywhere |
| D2.10 | WAF Configuration | Web Application Firewall | DevOps 2 | 8 | P0 | WAF protecting |

<details>
<summary><strong>D2.1 - Production Kubernetes Cluster (Detailed)</strong></summary>

```markdown
## Production Kubernetes Architecture

### Cluster Specifications

| Component | Specification |
|-----------|--------------|
| Provider | AWS EKS / GKE |
| Kubernetes Version | 1.28+ |
| Node Groups | 3 (system, application, monitoring) |
| Worker Nodes | 6-12 (auto-scaling) |
| Node Instance Type | m5.xlarge (4 vCPU, 16 GB) |
| Availability Zones | 3 (multi-AZ) |

### Node Groups

1. **System Node Group** (2 nodes)
   - Ingress controllers
   - Cert-manager
   - Cluster autoscaler
   - Metrics server

2. **Application Node Group** (3-8 nodes, auto-scaling)
   - All microservices
   - API Gateway
   - WebSocket servers

3. **Monitoring Node Group** (1-2 nodes)
   - Prometheus
   - Grafana
   - Elasticsearch
   - Jaeger

### Namespace Structure

```yaml
namespaces:
  - healthcare-prod        # Production services
  - healthcare-staging     # Staging environment
  - monitoring            # Observability stack
  - ingress               # Ingress controllers
  - cert-manager          # Certificate management
  - secrets               # External secrets
```

### High Availability Configuration

```yaml
# HPA for User Service (example)
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
  namespace: healthcare-prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Pod Disruption Budget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: user-service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: user-service
```
</details>

<details>
<summary><strong>D2.7 - Load Testing (Detailed)</strong></summary>

```markdown
## Load Testing Plan

### Tools
- k6 for API load testing
- Artillery for WebSocket testing
- JMeter for complex scenarios

### Test Scenarios

#### 1. Authentication Load Test
```javascript
// k6 script
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '2m', target: 100 },   // Ramp up
        { duration: '5m', target: 500 },   // Stay at 500 users
        { duration: '2m', target: 1000 },  // Peak load
        { duration: '5m', target: 1000 },  // Sustained peak
        { duration: '2m', target: 0 },     // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% under 500ms
        http_req_failed: ['rate<0.01'],    // <1% error rate
    },
};

export default function () {
    const res = http.post('https://api.healthapp.com/auth/login', {
        email: `user${__VU}@test.com`,
        password: 'testpassword',
    });
    
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });
    
    sleep(1);
}
```

#### 2. Doctor Search Load Test
- Target: 1000 concurrent searches
- Expected: p95 < 200ms
- Database: Elasticsearch

#### 3. Video Consultation Load Test
- Target: 200 concurrent video calls
- WebSocket connections: 400
- Twilio room stress test

### Performance Targets

| API | p50 | p95 | p99 | Max RPS |
|-----|-----|-----|-----|---------|
| Authentication | 100ms | 300ms | 500ms | 1000 |
| Doctor Search | 50ms | 200ms | 500ms | 2000 |
| Booking | 200ms | 500ms | 1000ms | 500 |
| Prescription | 300ms | 800ms | 1500ms | 200 |

### Chaos Engineering Tests
- Pod failure recovery
- Database failover
- Network partition
- Resource exhaustion
```
</details>

---

#### Security Tasks

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| S1.1 | Security Audit | Full code and config review | Security | 24 | P0 | Report generated |
| S1.2 | Vulnerability Fixes | Fix identified issues | Backend Team | 24 | P0 | All critical fixed |
| S1.3 | Penetration Testing | External pen test | External | 40 | P0 | Report clean |
| S1.4 | HIPAA Compliance | Compliance verification | Compliance | 20 | P0 | Checklist passed |
| S1.5 | Encryption Audit | Data at rest and in transit | Security | 12 | P0 | All encrypted |
| S1.6 | Access Control Review | RBAC and permissions | Security | 8 | P0 | ACL verified |
| S1.7 | Logging Audit | PHI access logging | Security | 8 | P0 | Audit logs complete |

<details>
<summary><strong>S1.4 - HIPAA Compliance Checklist</strong></summary>

```markdown
## HIPAA Compliance Checklist

### Administrative Safeguards

| Requirement | Implementation | Status |
|-------------|---------------|--------|
| Security Officer Designation | Assigned | ⬜ |
| Risk Analysis | Completed annually | ⬜ |
| Workforce Training | All staff trained | ⬜ |
| Access Management | RBAC implemented | ⬜ |
| Incident Response Plan | Documented | ⬜ |
| Business Associate Agreements | Signed with vendors | ⬜ |

### Physical Safeguards

| Requirement | Implementation | Status |
|-------------|---------------|--------|
| Facility Access Controls | Cloud provider certified | ⬜ |
| Workstation Security | VPN, encrypted | ⬜ |
| Device Controls | Mobile device management | ⬜ |

### Technical Safeguards

| Requirement | Implementation | Status |
|-------------|---------------|--------|
| Access Control | JWT + RBAC | ⬜ |
| Unique User ID | UUID per user | ⬜ |
| Automatic Logoff | Session timeout (30 min) | ⬜ |
| Encryption (transit) | TLS 1.3 | ⬜ |
| Encryption (rest) | AES-256 | ⬜ |
| Audit Controls | All PHI access logged | ⬜ |
| Integrity Controls | Checksums, backups | ⬜ |
| Authentication | MFA for admins, OTP for users | ⬜ |

### Documentation

| Document | Status |
|----------|--------|
| Security Policies | ⬜ |
| Privacy Policies | ⬜ |
| Incident Response Plan | ⬜ |
| Disaster Recovery Plan | ⬜ |
| Business Continuity Plan | ⬜ |
| Data Retention Policy | ⬜ |
```
</details>

---

#### QA Tasks

| ID | Task | Description | Assignee | Hours | Priority | Definition of Done |
|----|------|-------------|----------|-------|----------|-------------------|
| Q1.1 | E2E Testing | All user flows tested | QA Lead | 40 | P0 | All tests pass |
| Q1.2 | Mobile Testing | iOS and Android testing | QA 2 | 30 | P0 | Apps working |
| Q1.3 | Performance Testing | Response time, load | QA 1 | 20 | P0 | Targets met |
| Q1.4 | UAT Execution | Beta user testing | QA Lead | 24 | P0 | UAT signed off |
| Q1.5 | Bug Fixes | Fix UAT issues | All Teams | 40 | P0 | All P0/P1 fixed |
| Q1.6 | Regression Testing | Full regression | QA 1, 2 | 20 | P0 | No regressions |
| Q1.7 | Accessibility Testing | WCAG compliance | QA 1 | 12 | P1 | Accessible |

<details>
<summary><strong>Q1.1 - E2E Test Scenarios</strong></summary>

```markdown
## End-to-End Test Scenarios

### Patient Journey Tests

| # | Scenario | Priority |
|---|----------|----------|
| 1 | New patient registration with OTP | P0 |
| 2 | Patient login with password | P0 |
| 3 | Patient login with biometric | P0 |
| 4 | Search doctors by specialty | P0 |
| 5 | Search doctors by location | P0 |
| 6 | View doctor profile and reviews | P0 |
| 7 | Book video consultation | P0 |
| 8 | Complete payment for booking | P0 |
| 9 | Join video consultation | P0 |
| 10 | Complete video consultation | P0 |
| 11 | View and download prescription | P0 |
| 12 | Order medicines from prescription | P1 |
| 13 | Track medicine order | P1 |
| 14 | Book lab test with home collection | P1 |
| 15 | View lab report in health records | P1 |
| 16 | Submit doctor review | P0 |
| 17 | Cancel appointment (before cutoff) | P0 |
| 18 | Reschedule appointment | P1 |

### Doctor Journey Tests

| # | Scenario | Priority |
|---|----------|----------|
| 1 | Doctor login | P0 |
| 2 | View today's appointments | P0 |
| 3 | Start video consultation | P0 |
| 4 | Write prescription during consultation | P0 |
| 5 | Complete consultation | P0 |
| 6 | View patient history | P0 |
| 7 | Respond to patient review | P1 |
| 8 | Update availability | P0 |
| 9 | View earnings | P1 |

### Cross-Functional Tests

| # | Scenario | Priority |
|---|----------|----------|
| 1 | Complete consultation flow (patient + doctor) | P0 |
| 2 | Payment failure and retry | P0 |
| 3 | Network disconnection during video call | P0 |
| 4 | Concurrent bookings for same slot | P0 |
| 5 | Notification delivery (email, push, SMS) | P0 |
```
</details>

---

## Launch Checklist

### Pre-Launch (Week 26, Day 1-3)

| Task | Owner | Status |
|------|-------|--------|
| Production environment deployed | DevOps | ⬜ |
| All services health checks passing | DevOps | ⬜ |
| Database migrations complete | Backend Lead | ⬜ |
| SSL certificates installed and verified | DevOps | ⬜ |
| CDN configured and caching working | DevOps | ⬜ |
| WAF rules configured | DevOps | ⬜ |
| Monitoring dashboards ready | DevOps | ⬜ |
| Alert channels configured (PagerDuty/Slack) | DevOps | ⬜ |
| Runbooks reviewed by on-call team | DevOps | ⬜ |
| Backup and restore tested | DevOps | ⬜ |
| DR failover tested | DevOps | ⬜ |

### Launch Day (Week 26, Day 4)

| Task | Owner | Status |
|------|-------|--------|
| Final smoke tests | QA Lead | ⬜ |
| DNS switch to production | DevOps | ⬜ |
| Mobile apps submitted to stores | Mobile Lead | ⬜ |
| App Store approval received | Mobile Lead | ⬜ |
| Play Store approval received | Mobile Lead | ⬜ |
| Go/No-Go meeting | All Leads | ⬜ |
| **LAUNCH** 🚀 | All Teams | ⬜ |
| Monitor error rates (first 4 hours) | DevOps + Backend | ⬜ |
| Monitor performance metrics | DevOps | ⬜ |
| Customer support ready | Support Team | ⬜ |

### Post-Launch (Week 26, Day 5-7)

| Task | Owner | Status |
|------|-------|--------|
| Review Day 1 metrics | Tech Lead + PM | ⬜ |
| Address critical bugs | Dev Team | ⬜ |
| Scale infrastructure if needed | DevOps | ⬜ |
| Gather initial user feedback | PM | ⬜ |
| Plan hotfix release if needed | Tech Lead | ⬜ |
| Team retrospective | All Teams | ⬜ |
| Celebrate! 🎉 | Everyone | ⬜ |

---

## Phase 6 Milestone Checklist

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Doctor reviews system live | End of Week 23 | ⬜ |
| Health articles platform live | End of Week 24 | ⬜ |
| Patient mobile app alpha | End of Week 24 | ⬜ |
| Doctor mobile app alpha | End of Week 24 | ⬜ |
| Patient mobile app beta | End of Week 25 | ⬜ |
| Doctor mobile app beta | End of Week 25 | ⬜ |
| Production infrastructure ready | End of Week 25 | ⬜ |
| Security audit passed | End of Week 25 | ⬜ |
| HIPAA compliance verified | End of Week 26 | ⬜ |
| UAT completed successfully | End of Week 26 | ⬜ |
| App Store submission approved | End of Week 26 | ⬜ |
| Play Store submission approved | End of Week 26 | ⬜ |
| **🚀 PLATFORM LAUNCHED** | End of Week 26 | ⬜ |

---

## Definition of Done - Phase 6

- [ ] All tasks completed and code merged
- [ ] Unit test coverage ≥ 80%
- [ ] E2E tests passing
- [ ] Mobile apps submitted and approved
- [ ] Security audit passed with no critical issues
- [ ] Penetration test passed
- [ ] HIPAA compliance checklist complete
- [ ] Load tests meeting performance targets
- [ ] Production monitoring operational
- [ ] Runbooks documented
- [ ] UAT signed off by stakeholders
- [ ] All P0 bugs fixed
- [ ] Team trained on production support

---

## Post-Launch Roadmap

### Week 27-28: Stabilization
- Monitor production metrics
- Address user feedback
- Performance optimizations
- Bug fixes

### Month 2-3: Enhancements
- Additional payment methods
- Insurance integration
- Multi-language support
- Advanced analytics dashboard

### Month 4-6: Growth Features
- Corporate health programs
- Doctor referral system
- AI symptom checker
- Wearable integrations

---

## Appendix

### A. Key Contacts

| Role | Name | Contact |
|------|------|---------|
| Tech Lead | _TBD_ | tech-lead@healthapp.com |
| DevOps Lead | _TBD_ | devops@healthapp.com |
| Security Lead | _TBD_ | security@healthapp.com |
| QA Lead | _TBD_ | qa@healthapp.com |
| Product Manager | _TBD_ | pm@healthapp.com |
| On-Call | Rotation | oncall@healthapp.com |

### B. Incident Response

```markdown
## Severity Levels

| Severity | Description | Response Time | Example |
|----------|-------------|---------------|---------|
| SEV-1 | Platform down | 15 minutes | All services down |
| SEV-2 | Major feature broken | 1 hour | Video calls failing |
| SEV-3 | Minor feature issue | 4 hours | Search slow |
| SEV-4 | Cosmetic/Low impact | 24 hours | UI alignment |

## Escalation Path
1. On-call engineer
2. Team lead
3. Tech lead
4. CTO
```

### C. Rollback Procedures

```markdown
## Rollback Steps

1. Identify problematic deployment
2. Revert Kubernetes deployment:
   kubectl rollout undo deployment/<service> -n healthcare-prod
3. Verify rollback:
   kubectl rollout status deployment/<service>
4. Monitor for recovery
5. Create incident report
6. Plan fix for next release
```

---

*Document Version: 1.0*  
*Last Updated: January 30, 2026*  
*Author: Healthcare Platform Team*
