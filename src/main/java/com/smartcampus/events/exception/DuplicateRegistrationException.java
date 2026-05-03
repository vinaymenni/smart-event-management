package com.smartcampus.events.exception;

public class DuplicateRegistrationException extends RuntimeException {

    public DuplicateRegistrationException(String eventTitle) {
        super("You are already registered for event: " + eventTitle);
    }
}
