-- V2__seed_reference_data.sql

-- Seed Specializations
INSERT INTO specializations (id, name, description, display_order) VALUES
    (uuid_generate_v4(), 'General Physician', 'Primary care and general medicine', 1),
    (uuid_generate_v4(), 'Cardiologist', 'Heart and cardiovascular system', 2),
    (uuid_generate_v4(), 'Dermatologist', 'Skin, hair, and nail conditions', 3),
    (uuid_generate_v4(), 'Orthopedist', 'Bones, joints, and muscles', 4),
    (uuid_generate_v4(), 'Pediatrician', 'Child healthcare', 5),
    (uuid_generate_v4(), 'Gynecologist', 'Women health and reproductive system', 6),
    (uuid_generate_v4(), 'ENT Specialist', 'Ear, nose, and throat conditions', 7),
    (uuid_generate_v4(), 'Ophthalmologist', 'Eye care and vision', 8),
    (uuid_generate_v4(), 'Neurologist', 'Brain and nervous system', 9),
    (uuid_generate_v4(), 'Psychiatrist', 'Mental health and disorders', 10),
    (uuid_generate_v4(), 'Dentist', 'Dental and oral health', 11),
    (uuid_generate_v4(), 'Urologist', 'Urinary tract and male reproductive system', 12),
    (uuid_generate_v4(), 'Gastroenterologist', 'Digestive system', 13),
    (uuid_generate_v4(), 'Pulmonologist', 'Respiratory system and lungs', 14),
    (uuid_generate_v4(), 'Endocrinologist', 'Hormones and metabolic disorders', 15),
    (uuid_generate_v4(), 'Oncologist', 'Cancer treatment', 16),
    (uuid_generate_v4(), 'Nephrologist', 'Kidney conditions', 17),
    (uuid_generate_v4(), 'Rheumatologist', 'Arthritis and autoimmune diseases', 18),
    (uuid_generate_v4(), 'Physiotherapist', 'Physical therapy and rehabilitation', 19),
    (uuid_generate_v4(), 'Diabetologist', 'Diabetes management', 20);

-- Seed Languages
INSERT INTO languages (id, name, code) VALUES
    (uuid_generate_v4(), 'English', 'en'),
    (uuid_generate_v4(), 'Hindi', 'hi'),
    (uuid_generate_v4(), 'Bengali', 'bn'),
    (uuid_generate_v4(), 'Telugu', 'te'),
    (uuid_generate_v4(), 'Marathi', 'mr'),
    (uuid_generate_v4(), 'Tamil', 'ta'),
    (uuid_generate_v4(), 'Gujarati', 'gu'),
    (uuid_generate_v4(), 'Kannada', 'kn'),
    (uuid_generate_v4(), 'Malayalam', 'ml'),
    (uuid_generate_v4(), 'Punjabi', 'pa'),
    (uuid_generate_v4(), 'Odia', 'or'),
    (uuid_generate_v4(), 'Urdu', 'ur');
