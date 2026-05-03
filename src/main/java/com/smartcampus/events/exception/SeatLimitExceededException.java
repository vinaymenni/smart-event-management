package com.smartcampus.events.exception;

public class SeatLimitExceededException extends RuntimeException {

    public SeatLimitExceededException(String eventTitle) {
        super("No seats available for event: " + eventTitle);
    }
}
