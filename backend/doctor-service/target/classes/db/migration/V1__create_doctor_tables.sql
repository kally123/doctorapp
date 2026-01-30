-- V1__create_doctor_tables.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Specializations reference table
CREATE TABLE specializations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    parent_specialty_id UUID REFERENCES specializations(id),
    description TEXT,
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Languages reference table
CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE
);

-- Doctors table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,  -- References user-service
    
    -- Basic info
    full_name VARCHAR(200) NOT NULL,
    profile_picture_url VARCHAR(500),
    registration_number VARCHAR(100) UNIQUE,
    registration_council VARCHAR(200),
    experience_years INT,
    bio TEXT,
    
    -- Consultation fees
    consultation_fee DECIMAL(10, 2),
    video_consultation_fee DECIMAL(10, 2),
    followup_fee DECIMAL(10, 2),
    
    -- Ratings
    rating DECIMAL(3, 2) DEFAULT 0,
    review_count INT DEFAULT 0,
    
    -- Status
    is_verified BOOLEAN DEFAULT FALSE,
    is_accepting_patients BOOLEAN DEFAULT TRUE,
    verification_date TIMESTAMP WITH TIME ZONE,
    
    -- Profile completeness
    profile_completeness INT DEFAULT 0,  -- Percentage
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Doctor specializations (many-to-many)
CREATE TABLE doctor_specializations (
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    specialization_id UUID REFERENCES specializations(id),
    is_primary BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (doctor_id, specialization_id)
);

-- Doctor qualifications
CREATE TABLE doctor_qualifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    degree VARCHAR(100) NOT NULL,
    institution VARCHAR(200) NOT NULL,
    year_of_completion INT,
    certificate_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Doctor languages
CREATE TABLE doctor_languages (
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    language_id UUID REFERENCES languages(id),
    PRIMARY KEY (doctor_id, language_id)
);

-- Clinics
CREATE TABLE clinics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    address_line1 VARCHAR(500),
    address_line2 VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'India',
    postal_code VARCHAR(20),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Doctor-Clinic associations
CREATE TABLE doctor_clinics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    clinic_id UUID REFERENCES clinics(id) ON DELETE CASCADE,
    consultation_fee DECIMAL(10, 2),  -- Clinic-specific fee
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE (doctor_id, clinic_id)
);

-- Indexes
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_doctors_verified ON doctors(is_verified) WHERE is_verified = TRUE;
CREATE INDEX idx_doctors_accepting ON doctors(is_accepting_patients) WHERE is_accepting_patients = TRUE;
CREATE INDEX idx_doctors_rating ON doctors(rating);
CREATE INDEX idx_doctor_specs_doctor ON doctor_specializations(doctor_id);
CREATE INDEX idx_doctor_specs_spec ON doctor_specializations(specialization_id);
CREATE INDEX idx_doctor_quals_doctor ON doctor_qualifications(doctor_id);
CREATE INDEX idx_clinics_city ON clinics(city);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to doctors table
CREATE TRIGGER update_doctors_updated_at 
    BEFORE UPDATE ON doctors 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to clinics table
CREATE TRIGGER update_clinics_updated_at 
    BEFORE UPDATE ON clinics 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
