package com.smartcampus.events.exception;

public class RegistrationClosedException extends RuntimeException {

    public RegistrationClosedException(String eventTitle) {
        super("Registration is closed for event: " + eventTitle);
    }
}
