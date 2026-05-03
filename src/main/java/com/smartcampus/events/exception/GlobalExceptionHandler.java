package com.smartcampus.events.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(SeatLimitExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleSeatLimit(SeatLimitExceededException ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorCode", "409");
        model.addAttribute("errorTitle", "No Seats Available");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(RegistrationClosedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleRegistrationClosed(RegistrationClosedException ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "Registration Closed");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateRegistration(DuplicateRegistrationException ex, Model model,
            HttpServletRequest request) {
        model.addAttribute("errorCode", "409");
        model.addAttribute("errorTitle", "Already Registered");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        ex.printStackTrace();
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "Error: " + ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error";
    }
}
