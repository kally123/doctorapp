package com.healthapp.appointment.domain;

/**
 * Enum representing consultation types
 */
public enum ConsultationType {
    IN_PERSON("In-Person Visit"),
    VIDEO("Video Consultation"),
    AUDIO("Audio Consultation"),
    CHAT("Chat Consultation");
    
    private final String displayName;
    
    ConsultationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
