package com.smartcampus.events.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @NotBlank
    @Column(nullable = false)
    private String eventType; // Workshop, Seminar, Cultural, Sports, Technical, etc.

    @NotBlank
    private String venue;

    /**
     * Optional human-readable time slot (e.g., "10:00 AM - 12:00 PM").
     * This is additive and does not affect existing business logic.
     */
    private String timing;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate lastDateToApply;

    @Min(1)
    @Column(nullable = false)
    private Integer seatLimit;

    @Builder.Default
    @Min(1)
    @Column(nullable = false)
    private Integer minTeamSize = 1;

    @Builder.Default
    @Min(1)
    @Max(4)
    @Column(nullable = false)
    private Integer maxTeamSize = 1;

    @Builder.Default
    private Integer registeredCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (registeredCount == null)
            registeredCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Derive event status based on current date.
     */
    @Transient
    public EventStatus getStatus() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return EventStatus.UPCOMING;
        } else if (!today.isAfter(endDate)) {
            return EventStatus.ONGOING;
        } else {
            return EventStatus.COMPLETED;
        }
    }

    /**
     * Derive registration status based on last date to apply.
     */
    @Transient
    public boolean isRegistrationOpen() {
        return !LocalDate.now().isAfter(lastDateToApply);
    }

    @Transient
    public boolean isSeatsAvailable() {
        return registeredCount < seatLimit;
    }

    @Transient
    public int getAvailableSeats() {
        return seatLimit - registeredCount;
    }
}
