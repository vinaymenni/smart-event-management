package com.smartcampus.events.dto;

import com.smartcampus.events.model.Department;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 200, message = "Title must be 3–200 characters")
    private String title;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Department is required")
    private Department department;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotBlank(message = "Venue is required")
    private String venue;

    @Size(max = 100, message = "Timing too long")
    private String timing; // e.g., "10:00 AM - 12:00 PM"

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Last date to apply is required")
    private LocalDate lastDateToApply;

    @NotNull(message = "Seat limit is required")
    @Min(value = 1, message = "Seat limit must be at least 1")
    private Integer seatLimit;

    @NotNull(message = "Minimum team size is required")
    @Min(value = 1, message = "Minimum team size must be at least 1")
    @Max(value = 4, message = "Minimum team size cannot exceed 4")
    private Integer minTeamSize = 1;

    @NotNull(message = "Maximum team size is required")
    @Min(value = 1, message = "Maximum team size must be at least 1")
    @Max(value = 4, message = "Maximum team size cannot exceed 4")
    private Integer maxTeamSize = 1;
}
