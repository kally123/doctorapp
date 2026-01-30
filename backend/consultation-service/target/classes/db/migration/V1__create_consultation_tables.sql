-- V1__create_consultation_tables.sql
-- Consultation Service Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Consultation mode enum (stored as varchar for R2DBC compatibility)
-- Values: VIDEO, AUDIO, CHAT

-- Session status enum (stored as varchar for R2DBC compatibility)
-- Values: SCHEDULED, WAITING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED, NO_SHOW, FAILED

-- Main consultation sessions table
CREATE TABLE consultation_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- References
    appointment_id UUID NOT NULL UNIQUE,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    
    -- Session configuration
    consultation_mode VARCHAR(20) NOT NULL DEFAULT 'VIDEO',
    scheduled_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_duration_minutes INT NOT NULL DEFAULT 15,
    
    -- Video room details
    room_name VARCHAR(100),
    room_sid VARCHAR(100),
    
    -- Session tracking
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    
    -- Participant join times
    patient_joined_at TIMESTAMP WITH TIME ZONE,
    doctor_joined_at TIMESTAMP WITH TIME ZONE,
    
    -- Actual timing
    actual_start_time TIMESTAMP WITH TIME ZONE,
    actual_end_time TIMESTAMP WITH TIME ZONE,
    total_duration_seconds INT,
    
    -- Quality metrics
    patient_connection_quality VARCHAR(20),
    doctor_connection_quality VARCHAR(20),
    
    -- Recording (optional)
    is_recorded BOOLEAN DEFAULT FALSE,
    recording_url VARCHAR(500),
    recording_duration_seconds INT,
    
    -- Metadata
    end_reason VARCHAR(100),
    notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT valid_duration CHECK (scheduled_duration_minutes > 0 AND scheduled_duration_minutes <= 120),
    CONSTRAINT valid_mode CHECK (consultation_mode IN ('VIDEO', 'AUDIO', 'CHAT')),
    CONSTRAINT valid_status CHECK (status IN ('SCHEDULED', 'WAITING', 'IN_PROGRESS', 'PAUSED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'FAILED'))
);

-- Indexes for consultation_sessions
CREATE INDEX idx_sessions_appointment ON consultation_sessions(appointment_id);
CREATE INDEX idx_sessions_patient ON consultation_sessions(patient_id);
CREATE INDEX idx_sessions_doctor ON consultation_sessions(doctor_id);
CREATE INDEX idx_sessions_status ON consultation_sessions(status);
CREATE INDEX idx_sessions_scheduled_time ON consultation_sessions(scheduled_start_time);
CREATE INDEX idx_sessions_room ON consultation_sessions(room_name);

-- Participant tracking table
CREATE TABLE session_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES consultation_sessions(id) ON DELETE CASCADE,
    
    user_id UUID NOT NULL,
    participant_type VARCHAR(20) NOT NULL,
    
    -- Connection events
    joined_at TIMESTAMP WITH TIME ZONE,
    left_at TIMESTAMP WITH TIME ZONE,
    rejoin_count INT DEFAULT 0,
    
    -- Device info
    device_type VARCHAR(50),
    browser VARCHAR(100),
    os VARCHAR(100),
    
    -- Connection quality log
    avg_audio_level FLOAT,
    avg_video_bitrate INT,
    packet_loss_percent FLOAT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_participant UNIQUE (session_id, user_id),
    CONSTRAINT valid_participant_type CHECK (participant_type IN ('PATIENT', 'DOCTOR'))
);

CREATE INDEX idx_participants_session ON session_participants(session_id);
CREATE INDEX idx_participants_user ON session_participants(user_id);

-- Session events log (for analytics and debugging)
CREATE TABLE session_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES consultation_sessions(id) ON DELETE CASCADE,
    
    event_type VARCHAR(50) NOT NULL,
    user_id UUID,
    event_data JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_session_events_session ON session_events(session_id);
CREATE INDEX idx_session_events_type ON session_events(event_type);
CREATE INDEX idx_session_events_created ON session_events(created_at);

-- Consultation pricing table
CREATE TABLE consultation_pricing (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    consultation_mode VARCHAR(20) NOT NULL,
    
    -- Pricing model: FLAT, PER_MINUTE, TIERED
    pricing_type VARCHAR(20) NOT NULL DEFAULT 'FLAT',
    
    -- For FLAT pricing
    flat_fee DECIMAL(10, 2),
    
    -- For PER_MINUTE pricing
    per_minute_rate DECIMAL(10, 2),
    minimum_minutes INT DEFAULT 5,
    
    -- For TIERED pricing
    tier_config JSONB,
    
    currency VARCHAR(3) DEFAULT 'INR',
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_doctor_mode UNIQUE (doctor_id, consultation_mode),
    CONSTRAINT valid_pricing_type CHECK (pricing_type IN ('FLAT', 'PER_MINUTE', 'TIERED'))
);

CREATE INDEX idx_pricing_doctor ON consultation_pricing(doctor_id);

-- Consultation feedback table
CREATE TABLE consultation_feedback (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL UNIQUE REFERENCES consultation_sessions(id) ON DELETE CASCADE,
    
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    
    -- Ratings (1-5 scale)
    overall_rating INT CHECK (overall_rating BETWEEN 1 AND 5),
    video_quality_rating INT CHECK (video_quality_rating BETWEEN 1 AND 5),
    audio_quality_rating INT CHECK (audio_quality_rating BETWEEN 1 AND 5),
    doctor_rating INT CHECK (doctor_rating BETWEEN 1 AND 5),
    
    -- Written feedback
    review_text TEXT,
    
    -- Tags
    tags VARCHAR(255)[],
    
    -- Would recommend
    would_recommend BOOLEAN,
    
    -- Technical issues reported
    had_technical_issues BOOLEAN DEFAULT FALSE,
    technical_issue_description TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_feedback_session ON consultation_feedback(session_id);
CREATE INDEX idx_feedback_doctor ON consultation_feedback(doctor_id);
CREATE INDEX idx_feedback_rating ON consultation_feedback(overall_rating);

-- Trigger to update updated_at on consultation_sessions
CREATE OR REPLACE FUNCTION update_consultation_session_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER consultation_session_updated
    BEFORE UPDATE ON consultation_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_consultation_session_timestamp();

CREATE TRIGGER consultation_pricing_updated
    BEFORE UPDATE ON consultation_pricing
    FOR EACH ROW
    EXECUTE FUNCTION update_consultation_session_timestamp();
