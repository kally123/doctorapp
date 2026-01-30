-- V1__create_payment_tables.sql

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Payment status enum
CREATE TYPE payment_status AS ENUM (
    'INITIATED',
    'PENDING',
    'AUTHORIZED',
    'CAPTURED',
    'COMPLETED',
    'FAILED',
    'CANCELLED',
    'REFUND_INITIATED',
    'PARTIALLY_REFUNDED',
    'REFUNDED'
);

-- Payment method enum
CREATE TYPE payment_method AS ENUM (
    'CARD',
    'UPI',
    'NET_BANKING',
    'WALLET',
    'EMI',
    'PAY_LATER'
);

-- Payment transactions table
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Reference
    order_type VARCHAR(50) NOT NULL,
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    
    -- Amount
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    
    -- Payment details
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    
    -- Gateway details
    gateway VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY',
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    gateway_signature VARCHAR(255),
    
    -- Response
    gateway_response JSONB,
    failure_reason TEXT,
    
    -- Refund tracking
    refunded_amount DECIMAL(10, 2) DEFAULT 0,
    
    -- Metadata
    description TEXT,
    metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    
    -- Idempotency
    idempotency_key VARCHAR(100) UNIQUE
);

-- Refunds table
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL REFERENCES payment_transactions(id),
    
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    
    reason VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    
    -- Gateway
    gateway_refund_id VARCHAR(100),
    gateway_response JSONB,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Doctor payouts table
CREATE TABLE doctor_payouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID NOT NULL,
    
    -- Period
    payout_period_start DATE NOT NULL,
    payout_period_end DATE NOT NULL,
    
    -- Amount
    gross_amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) NOT NULL,
    tax_deducted DECIMAL(10, 2) DEFAULT 0,
    net_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    
    -- Status
    status VARCHAR(50) DEFAULT 'PENDING',
    
    -- Bank details
    bank_account_id UUID,
    
    -- Gateway
    payout_reference VARCHAR(100),
    gateway_response JSONB,
    
    -- Appointments included
    appointment_count INT,
    appointment_ids UUID[],
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,
    
    UNIQUE(doctor_id, payout_period_start, payout_period_end)
);

-- Indexes
CREATE INDEX idx_payments_order ON payment_transactions(order_type, order_id);
CREATE INDEX idx_payments_user ON payment_transactions(user_id);
CREATE INDEX idx_payments_status ON payment_transactions(status);
CREATE INDEX idx_payments_gateway_order ON payment_transactions(gateway_order_id);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
CREATE INDEX idx_payouts_doctor ON doctor_payouts(doctor_id, status);
