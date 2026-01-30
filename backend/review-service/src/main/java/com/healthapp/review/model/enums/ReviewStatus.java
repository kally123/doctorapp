package com.healthapp.review.model.enums;

public enum ReviewStatus {
    PENDING,      // Awaiting moderation
    APPROVED,     // Published
    REJECTED,     // Rejected by moderation
    FLAGGED,      // Flagged for review
    HIDDEN        // Hidden by admin
}
