package com.healthapp.appointment.domain;

/**
 * Enum representing appointment statuses
 */
public enum AppointmentStatus {
    PENDING_PAYMENT("Pending Payment", false, true),
    PAYMENT_FAILED("Payment Failed", false, false),
    CONFIRMED("Confirmed", true, true),
    REMINDER_SENT("Reminder Sent", true, true),
    CHECKED_IN("Checked In", true, true),
    IN_PROGRESS("In Progress", true, true),
    COMPLETED("Completed", false, false),
    CANCELLED_BY_PATIENT("Cancelled by Patient", false, false),
    CANCELLED_BY_DOCTOR("Cancelled by Doctor", false, false),
    CANCELLED_SYSTEM("System Cancelled", false, false),
    NO_SHOW("No Show", false, false),
    RESCHEDULED("Rescheduled", false, false);
    
    private final String displayName;
    private final boolean active;
    private final boolean cancellable;
    
    AppointmentStatus(String displayName, boolean active, boolean cancellable) {
        this.displayName = displayName;
        this.active = active;
        this.cancellable = cancellable;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public boolean isCancellable() {
        return cancellable;
    }
}
