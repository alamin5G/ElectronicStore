package com.goonok.electronicstore.enums;

public enum ContactMessageStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_RESPONSE("Waiting for Response"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    ContactMessageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}