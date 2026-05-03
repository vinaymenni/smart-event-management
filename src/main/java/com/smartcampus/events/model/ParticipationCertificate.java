package com.smartcampus.events.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String certificateCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false, unique = true)
    private Registration registration;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }
}

