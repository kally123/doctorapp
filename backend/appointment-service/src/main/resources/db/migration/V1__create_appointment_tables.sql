-- V1__create_appointment_tables.sql

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Consultation type enum
CREATE TYPE consultation_type AS ENUM (
    'IN_PERSON',
    'VIDEO',
    'AUDIO',
    'CHAT'
);

-- Appointment status enum
CREATE TYPE appointment_status AS ENUM (
    'PENDING_PAYMENT',
    'PAYMENT_FAILED',
    'CONFIRMED',
    'REMINDER_SENT',
    'CHECKED_IN',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED_BY_PATIENT',
    'CANCELLED_BY_DOCTOR',
    'CANCELLED_SYSTEM',
    'NO_SHOW',
    'RESCHEDULED'
);

-- Slot status enum
CREATE TYPE slot_status AS ENUM (
    'AVAILABLE',
    'RESERVED',
    'BOOKED',
    'BLOCKED'
);

-- Weekly availability table
CREATE TABLE weekly_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    clinic_id UUID,
    
    day_of_week INT NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    slot_duration_minutes INT NOT NULL DEFAULT 15,
    buffer_minutes INT DEFAULT 5,
    
    consultation_type consultation_type NOT NULL,
    max_patients_per_slot INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_time_range CHECK (start_time < end_time),
    CONSTRAINT unique_doctor_day_time UNIQUE (doctor_id, clinic_id, day_of_week, start_time, consultation_type)
);

-- Availability overrides (date-specific changes)
CREATE TABLE availability_overrides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    override_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    reason VARCHAR(255),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_override UNIQUE (doctor_id, override_date)
);

-- Blocked slots (leave, unavailable)
CREATE TABLE blocked_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    start_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    
    reason VARCHAR(255),
    block_type VARCHAR(50) DEFAULT 'LEAVE',
    
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_block_range CHECK (start_datetime < end_datetime)
);

-- Available slots (generated from weekly availability)
CREATE TABLE available_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    clinic_id UUID,
    
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    consultation_type consultation_type NOT NULL,
    slot_duration_minutes INT NOT NULL,
    
    status slot_status DEFAULT 'AVAILABLE',
    appointment_id UUID,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT unique_slot UNIQUE (doctor_id, slot_date, start_time, clinic_id)
);

-- Appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    clinic_id UUID,
    
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 15,
    
    consultation_type consultation_type NOT NULL,
    status appointment_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    
    slot_id UUID REFERENCES available_slots(id),
    
    consultation_fee DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) DEFAULT 0,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    
    payment_id UUID,
    payment_status VARCHAR(50),
    
    booking_notes TEXT,
    
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancelled_by UUID,
    cancellation_reason TEXT,
    refund_amount DECIMAL(10, 2),
    refund_status VARCHAR(50),
    
    rescheduled_from_id UUID REFERENCES appointments(id),
    rescheduled_to_id UUID REFERENCES appointments(id),
    reschedule_count INT DEFAULT 0,
    
    consultation_started_at TIMESTAMP WITH TIME ZONE,
    consultation_ended_at TIMESTAMP WITH TIME ZONE,
    consultation_id UUID,
    
    is_followup BOOLEAN DEFAULT FALSE,
    original_appointment_id UUID REFERENCES appointments(id),
    followup_scheduled BOOLEAN DEFAULT FALSE,
    
    reserved_until TIMESTAMP WITH TIME ZONE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT valid_reservation CHECK (
        status != 'PENDING_PAYMENT' OR reserved_until IS NOT NULL
    )
);

-- Appointment status history (audit trail)
CREATE TABLE appointment_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    from_status appointment_status,
    to_status appointment_status NOT NULL,
    changed_by UUID,
    reason TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Scheduled reminders
CREATE TABLE scheduled_reminders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    
    reminder_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    scheduled_for TIMESTAMP WITH TIME ZONE NOT NULL,
    
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_weekly_availability_doctor ON weekly_availability(doctor_id) WHERE is_active = TRUE;
CREATE INDEX idx_weekly_availability_day ON weekly_availability(day_of_week) WHERE is_active = TRUE;
CREATE INDEX idx_availability_overrides_doctor_date ON availability_overrides(doctor_id, override_date);
CREATE INDEX idx_blocked_slots_doctor ON blocked_slots(doctor_id, start_datetime, end_datetime);
CREATE INDEX idx_available_slots_doctor_date ON available_slots(doctor_id, slot_date) WHERE status = 'AVAILABLE';
CREATE INDEX idx_available_slots_status ON available_slots(status, slot_date);

CREATE INDEX idx_appointments_patient ON appointments(patient_id, scheduled_at);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id, scheduled_at);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_scheduled ON appointments(scheduled_at);
CREATE INDEX idx_appointments_pending ON appointments(status, reserved_until) 
    WHERE status = 'PENDING_PAYMENT';
CREATE INDEX idx_appointments_upcoming ON appointments(scheduled_at) 
    WHERE status IN ('CONFIRMED', 'REMINDER_SENT');
CREATE INDEX idx_status_history_appointment ON appointment_status_history(appointment_id);
CREATE INDEX idx_scheduled_reminders_due ON scheduled_reminders(scheduled_for, status)
    WHERE status = 'PENDING';
