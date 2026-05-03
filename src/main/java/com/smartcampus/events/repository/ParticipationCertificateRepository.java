package com.smartcampus.events.repository;

import com.smartcampus.events.model.ParticipationCertificate;
import com.smartcampus.events.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipationCertificateRepository extends JpaRepository<ParticipationCertificate, Long> {
    Optional<ParticipationCertificate> findByRegistration(Registration registration);
}

