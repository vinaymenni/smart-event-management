package com.smartcampus.events.model;

public enum Department {
    CSE("Computer Science & Engineering"),
    ECE("Electronics & Communication Engineering"),
    EEE("Electrical & Electronics Engineering"),
    MECH("Mechanical Engineering"),
    CIVIL("Civil Engineering"),
    IT("Information Technology");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
