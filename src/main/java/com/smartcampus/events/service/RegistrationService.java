package com.smartcampus.events.service;

import com.smartcampus.events.exception.*;
import com.smartcampus.events.model.*;
import com.smartcampus.events.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final EmailService emailService;

    @Transactional
    public Registration registerForEvent(User student, Long eventId) {
        return registerForEvent(student, eventId, null, null, null, null, null);
    }

    @Transactional
    public Registration registerForEvent(User student, Long eventId, String email, String phone,
            List<String> memberNames, List<String> memberEmails, List<String> memberPhones) {
        Event event = eventService.findById(eventId);

        if (!event.isRegistrationOpen()) {
            throw new RegistrationClosedException(event.getTitle());
        }
        if (!event.isSeatsAvailable()) {
            throw new SeatLimitExceededException(event.getTitle());
        }
        if (registrationRepository.existsByStudentAndEvent(student, event)) {
            throw new DuplicateRegistrationException(event.getTitle());
        }

        String contactEmail = normalizeEmail(email);
        if (contactEmail == null) {
            contactEmail = normalizeEmail(student.getEmail());
        }
        if (contactEmail == null) {
            throw new IllegalArgumentException("Email is required.");
        }

        String contactPhone = normalizePhone(phone);
        if (contactPhone == null) {
            contactPhone = normalizePhone(student.getPhone());
        }

        List<RegistrationTeamMember> teamMembers = buildTeamMembers(student, memberNames, memberEmails, memberPhones);
        int teamSize = teamMembers.size();
        int minTeamSize = event.getMinTeamSize() != null ? event.getMinTeamSize() : 1;
        int maxTeamSize = event.getMaxTeamSize() != null ? event.getMaxTeamSize() : 1;
        if (minTeamSize > maxTeamSize) {
            throw new IllegalArgumentException("Invalid team size settings for this event.");
        }
        if (teamSize < minTeamSize || teamSize > maxTeamSize) {
            throw new IllegalArgumentException("Team size must be between " + minTeamSize + " and " + maxTeamSize + ".");
        }

        Registration registration = Registration.builder()
                .student(student)
                .event(event)
                .ticketCode("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .contactEmail(contactEmail)
                .contactPhone(contactPhone)
                .status(RegistrationStatus.REGISTERED)
                .teamMembers(new ArrayList<>())
                .build();
        for (RegistrationTeamMember member : teamMembers) {
            member.setRegistration(registration);
            registration.getTeamMembers().add(member);
        }

        eventService.incrementRegistrationCount(event);
        try {
            registration = registrationRepository.save(registration);
        } catch (DataIntegrityViolationException | PessimisticLockingFailureException ex) {
            // Covers unique(student,event) collisions and concurrent double-submit cases.
            throw new DuplicateRegistrationException(event.getTitle());
        }

        emailService.sendRegistrationConfirmation(registration);

        return registration;
    }

    @Transactional
    public void cancelRegistration(User student, Long registrationId) {
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", registrationId));

        if (!reg.getStudent().getId().equals(student.getId())) {
            throw new ResourceNotFoundException("Registration", registrationId);
        }
        if (reg.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already cancelled.");
        }

        reg.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(reg);
        eventService.decrementRegistrationCount(reg.getEvent());
    }

    public List<Registration> findByStudent(User student) {
        return registrationRepository.findByStudentWithDetails(student);
    }

    public List<Registration> findByStudentActive(User student) {
        return registrationRepository.findByStudentAndStatus(student, RegistrationStatus.REGISTERED);
    }

    public List<Registration> findByEvent(Event event) {
        return registrationRepository.findByEvent(event);
    }

    public Registration findByIdWithDetails(Long id) {
        return registrationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", id));
    }

    public boolean isStudentRegistered(User student, Event event) {
        return registrationRepository.existsByStudentAndEvent(student, event);
    }

    public long countByEvent(Event event) {
        return registrationRepository.countByEventAndStatus(event, RegistrationStatus.REGISTERED);
    }

    public List<Object[]> countRegistrationsByEvent() {
        return registrationRepository.countRegistrationsByEvent(RegistrationStatus.REGISTERED);
    }

    public List<Object[]> countRegistrationsByDepartment() {
        return registrationRepository.countRegistrationsByDepartment(RegistrationStatus.REGISTERED);
    }

    public long countTotalRegistrations() {
        return registrationRepository.countTotalActiveRegistrations(RegistrationStatus.REGISTERED);
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        String v = email.trim();
        if (v.isEmpty()) return null;
        try {
            InternetAddress addr = new InternetAddress(v);
            addr.validate();
            return v;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String v = phone.trim();
        if (v.isEmpty()) return null;
        if (!v.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("Phone must be 10 digits.");
        }
        return v;
    }

    private static List<RegistrationTeamMember> buildTeamMembers(User student, List<String> memberNames,
            List<String> memberEmails, List<String> memberPhones) {
        List<RegistrationTeamMember> members = new ArrayList<>();
        // Include the registering student as member 1
        members.add(RegistrationTeamMember.builder()
                .name(student.getName())
                .email(student.getEmail())
                .phone(student.getPhone() != null ? student.getPhone() : "N/A")
                .build());

        List<String> names = memberNames == null ? Collections.emptyList() : memberNames;
        List<String> emails = memberEmails == null ? Collections.emptyList() : memberEmails;
        List<String> phones = memberPhones == null ? Collections.emptyList() : memberPhones;
        int max = Math.max(names.size(), Math.max(emails.size(), phones.size()));

        for (int i = 0; i < max; i++) {
            String n = i < names.size() ? names.get(i) : null;
            String e = i < emails.size() ? emails.get(i) : null;
            String p = i < phones.size() ? phones.get(i) : null;
            boolean allEmpty = isBlank(n) && isBlank(e) && isBlank(p);
            if (allEmpty) {
                continue;
            }
            if (isBlank(n) || isBlank(e) || isBlank(p)) {
                throw new IllegalArgumentException("For each teammate, name, email and phone are required.");
            }
            RegistrationTeamMember teammate = RegistrationTeamMember.builder()
                    .name(n.trim())
                    .email(normalizeEmail(e))
                    .phone(normalizePhone(p))
                    .build();
            members.add(teammate);
        }
        return members;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
