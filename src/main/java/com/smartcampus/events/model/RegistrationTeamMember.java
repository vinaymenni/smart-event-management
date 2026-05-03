package com.smartcampus.events.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "registration_team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;
}

