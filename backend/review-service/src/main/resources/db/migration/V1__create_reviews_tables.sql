-- V1__create_reviews_tables.sql
-- Review Service Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Review status enum
CREATE TYPE review_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'FLAGGED',
    'HIDDEN'
);

-- Vote type enum
CREATE TYPE vote_type AS ENUM (
    'HELPFUL',
    'NOT_HELPFUL'
);

-- Report reason enum
CREATE TYPE report_reason AS ENUM (
    'SPAM',
    'FAKE',
    'INAPPROPRIATE',
    'HARASSMENT',
    'PRIVACY_VIOLATION',
    'OTHER'
);

-- Report status enum
CREATE TYPE report_status AS ENUM (
    'PENDING',
    'REVIEWED',
    'ACTIONED',
    'DISMISSED'
);

-- Consultation type enum
CREATE TYPE consultation_type AS ENUM (
    'IN_PERSON',
    'VIDEO',
    'AUDIO'
);

-- Main reviews table
CREATE TABLE doctor_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- References
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    consultation_id UUID,
    appointment_id UUID,
    
    -- Ratings (1-5 scale)
    overall_rating INT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    wait_time_rating INT CHECK (wait_time_rating BETWEEN 1 AND 5),
    bedside_manner_rating INT CHECK (bedside_manner_rating BETWEEN 1 AND 5),
    explanation_rating INT CHECK (explanation_rating BETWEEN 1 AND 5),
    
    -- Review content
    title VARCHAR(200),
    review_text TEXT,
    
    -- Consultation type reviewed
    consultation_type consultation_type,
    
    -- Tags (comma-separated)
    positive_tags TEXT,
    improvement_tags TEXT,
    
    -- Verification
    is_verified BOOLEAN DEFAULT TRUE,
    
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
    
    -- Prevent duplicate reviews per consultation
    CONSTRAINT unique_patient_consultation UNIQUE (patient_id, consultation_id)
);

-- Indexes for reviews
CREATE INDEX idx_reviews_doctor ON doctor_reviews(doctor_id);
CREATE INDEX idx_reviews_patient ON doctor_reviews(patient_id);
CREATE INDEX idx_reviews_status ON doctor_reviews(status);
CREATE INDEX idx_reviews_rating ON doctor_reviews(overall_rating);
CREATE INDEX idx_reviews_created ON doctor_reviews(created_at DESC);
CREATE INDEX idx_reviews_doctor_status ON doctor_reviews(doctor_id, status);

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
    
    -- Top tags (JSON string)
    top_positive_tags TEXT,
    top_improvement_tags TEXT,
    
    -- Recommendation rate
    recommendation_rate DECIMAL(5, 2),
    
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Review votes
CREATE TABLE review_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL REFERENCES doctor_reviews(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    vote_type vote_type NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_user_review_vote UNIQUE (review_id, user_id)
);

CREATE INDEX idx_votes_review ON review_votes(review_id);
CREATE INDEX idx_votes_user ON review_votes(user_id);

-- Review reports
CREATE TABLE review_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id UUID NOT NULL REFERENCES doctor_reviews(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL,
    
    reason report_reason NOT NULL,
    description TEXT,
    
    status report_status DEFAULT 'PENDING',
    reviewed_by UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    action_taken VARCHAR(200),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_reporter_review UNIQUE (review_id, reporter_id)
);

CREATE INDEX idx_reports_review ON review_reports(review_id);
CREATE INDEX idx_reports_status ON review_reports(status);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_reviews_updated_at
    BEFORE UPDATE ON doctor_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data for testing
INSERT INTO doctor_rating_aggregates (doctor_id, average_rating, total_reviews)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 4.8, 125),
    ('22222222-2222-2222-2222-222222222222', 4.5, 89),
    ('33333333-3333-3333-3333-333333333333', 4.9, 234);
