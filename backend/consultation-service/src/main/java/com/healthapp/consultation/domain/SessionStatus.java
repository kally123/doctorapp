package com.healthapp.consultation.domain;

/**
 * Status of a consultation session throughout its lifecycle.
 */
public enum SessionStatus {
    SCHEDULED,      // Consultation is scheduled
    WAITING,        // Patient in waiting room
    IN_PROGRESS,    // Active consultation
    PAUSED,         // Temporarily paused
    COMPLETED,      // Successfully completed
    CANCELLED,      // Cancelled before start
    NO_SHOW,        // Patient/Doctor didn't join
    FAILED          // Technical failure
}
