-- V1__create_notification_tables.sql

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Notification channel enum
CREATE TYPE notification_channel AS ENUM (
    'EMAIL',
    'SMS',
    'PUSH',
    'IN_APP'
);

-- Notification type enum
CREATE TYPE notification_type AS ENUM (
    'BOOKING_CONFIRMED',
    'BOOKING_CANCELLED',
    'APPOINTMENT_REMINDER',
    'PAYMENT_SUCCESS',
    'PAYMENT_FAILED',
    'REFUND_PROCESSED',
    'DOCTOR_REGISTRATION',
    'VERIFICATION_COMPLETE',
    'PASSWORD_RESET',
    'OTP',
    'PROMOTIONAL'
);

-- Notification status enum
CREATE TYPE notification_status AS ENUM (
    'PENDING',
    'SENT',
    'DELIVERED',
    'FAILED',
    'BOUNCED'
);

-- Notification templates table
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    
    subject VARCHAR(255),
    body_template TEXT NOT NULL,
    
    locale VARCHAR(10) DEFAULT 'en',
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(notification_type, channel, locale)
);

-- Notification logs table
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- Reference
    reference_type VARCHAR(50),
    reference_id UUID,
    
    -- Response
    provider_response TEXT,
    error_message TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE
);

-- Scheduled notifications table
CREATE TABLE scheduled_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    
    reference_type VARCHAR(50),
    reference_id UUID,
    
    scheduled_for TIMESTAMP WITH TIME ZONE NOT NULL,
    
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    context JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User notification preferences table
CREATE TABLE user_notification_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    
    marketing_email BOOLEAN DEFAULT FALSE,
    marketing_sms BOOLEAN DEFAULT FALSE,
    
    -- Reminder preferences
    reminder_24h BOOLEAN DEFAULT TRUE,
    reminder_1h BOOLEAN DEFAULT TRUE,
    reminder_15min BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User devices for push notifications
CREATE TABLE user_devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    
    device_token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    device_name VARCHAR(100),
    
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_used_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    UNIQUE(user_id, device_token)
);

-- Indexes
CREATE INDEX idx_notification_logs_user ON notification_logs(user_id, created_at DESC);
CREATE INDEX idx_notification_logs_status ON notification_logs(status, created_at);
CREATE INDEX idx_notification_logs_reference ON notification_logs(reference_type, reference_id);
CREATE INDEX idx_scheduled_notifications_due ON scheduled_notifications(scheduled_for, status)
    WHERE status = 'PENDING';
CREATE INDEX idx_user_devices_user ON user_devices(user_id) WHERE is_active = TRUE;

-- Insert default templates
INSERT INTO notification_templates (name, notification_type, channel, subject, body_template) VALUES
('Booking Confirmed Email', 'BOOKING_CONFIRMED', 'EMAIL', 
 'Appointment Confirmed - {{doctorName}}', 
 'Dear {{patientName}}, Your appointment with Dr. {{doctorName}} has been confirmed for {{appointmentDate}} at {{appointmentTime}}. Booking ID: {{appointmentId}}'),

('Booking Confirmed SMS', 'BOOKING_CONFIRMED', 'SMS', NULL,
 'Your appointment with Dr. {{doctorName}} is confirmed for {{appointmentDate}} at {{appointmentTime}}. ID: {{appointmentId}}'),

('Appointment Reminder Email', 'APPOINTMENT_REMINDER', 'EMAIL',
 'Reminder: Appointment in {{timeUntil}}',
 'Dear {{patientName}}, This is a reminder for your appointment with Dr. {{doctorName}} in {{timeUntil}}. Please be on time.'),

('Appointment Reminder SMS', 'APPOINTMENT_REMINDER', 'SMS', NULL,
 'Reminder: Your appointment with Dr. {{doctorName}} is in {{timeUntil}}. Booking ID: {{appointmentId}}'),

('Booking Cancelled Email', 'BOOKING_CANCELLED', 'EMAIL',
 'Appointment Cancelled',
 'Dear {{patientName}}, Your appointment with Dr. {{doctorName}} scheduled for {{appointmentDate}} has been cancelled. Reason: {{cancellationReason}}'),

('Payment Success Email', 'PAYMENT_SUCCESS', 'EMAIL',
 'Payment Successful - ₹{{amount}}',
 'Dear {{patientName}}, Your payment of ₹{{amount}} has been successfully processed. Transaction ID: {{transactionId}}'),

('Refund Processed Email', 'REFUND_PROCESSED', 'EMAIL',
 'Refund Processed - ₹{{amount}}',
 'Dear {{patientName}}, Your refund of ₹{{amount}} has been processed. It will be credited to your account within 5-7 business days.');
