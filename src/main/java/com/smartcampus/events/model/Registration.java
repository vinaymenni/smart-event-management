package com.smartcampus.events.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "registrations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "event_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(unique = true, nullable = false)
    private String ticketCode;

    @Column(nullable = false)
    private String contactEmail;

    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RegistrationTeamMember> teamMembers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        if (status == null) status = RegistrationStatus.REGISTERED;
    }
}
