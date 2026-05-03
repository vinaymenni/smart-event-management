package com.smartcampus.events.config;

import com.smartcampus.events.model.*;
import com.smartcampus.events.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin
        if (!userRepository.existsByEmail("admin@campus.edu")) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@campus.edu")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .department(Department.CSE)
                    .phone("9999999999")
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@campus.edu / admin123");

            // Create demo student
            User student = User.builder()
                    .name("Demo Student")
                    .email("student@campus.edu")
                    .username("demo.student")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .department(Department.CSE)
                    .phone("8888888888")
                    .build();
            userRepository.save(student);
            log.info("Demo student created: student@campus.edu / student123");

            // Create sample events
            createSampleEvents(admin);
        }
    }

    private void createSampleEvents(User admin) {
        LocalDate today = LocalDate.now();

        Event e1 = Event.builder()
                .title("National CodeSprint 2026")
                .description(
                        "A 48-hour competitive programming marathon open to all engineering students. Solve algorithmic challenges and win prizes.")
                .department(Department.CSE)
                .eventType("Technical")
                .venue("CS Lab Block A")
                .startDate(today.plusDays(10))
                .endDate(today.plusDays(12))
                .lastDateToApply(today.plusDays(7))
                .seatLimit(150)
                .registeredCount(0)
                .createdBy(admin)
                .build();

        Event e2 = Event.builder()
                .title("IoT & Embedded Systems Workshop")
                .description(
                        "Hands-on workshop on building IoT devices using Raspberry Pi and Arduino with real-world sensor integration.")
                .department(Department.ECE)
                .eventType("Workshop")
                .venue("ECE Innovation Hub")
                .startDate(today.plusDays(5))
                .endDate(today.plusDays(6))
                .lastDateToApply(today.plusDays(3))
                .seatLimit(60)
                .registeredCount(0)
                .createdBy(admin)
                .build();

        Event e3 = Event.builder()
                .title("Green Energy Symposium")
                .description(
                        "A seminar on renewable energy trends, solar panel tech, and sustainable engineering practices.")
                .department(Department.EEE)
                .eventType("Seminar")
                .venue("Seminar Hall 2")
                .startDate(today.minusDays(2))
                .endDate(today.plusDays(1))
                .lastDateToApply(today.minusDays(5))
                .seatLimit(200)
                .registeredCount(0)
                .createdBy(admin)
                .build();

        Event e4 = Event.builder()
                .title("Robotics Design Challenge")
                .description(
                        "Teams design and build autonomous robots to complete obstacle courses. Cash prizes for top 3 teams.")
                .department(Department.MECH)
                .eventType("Competition")
                .venue("Mechanical Workshop")
                .startDate(today.plusDays(20))
                .endDate(today.plusDays(21))
                .lastDateToApply(today.plusDays(15))
                .seatLimit(80)
                .registeredCount(0)
                .createdBy(admin)
                .build();

        Event e5 = Event.builder()
                .title("AI in Healthcare – Guest Lecture")
                .description(
                        "Industry experts discuss the role of machine learning and AI models in diagnostics, imaging, and patient care.")
                .department(Department.IT)
                .eventType("Guest Lecture")
                .venue("Auditorium")
                .startDate(today.minusDays(10))
                .endDate(today.minusDays(10))
                .lastDateToApply(today.minusDays(12))
                .seatLimit(300)
                .registeredCount(0)
                .createdBy(admin)
                .build();

        eventRepository.save(e1);
        eventRepository.save(e2);
        eventRepository.save(e3);
        eventRepository.save(e4);
        eventRepository.save(e5);
        log.info("Sample events created successfully.");
    }
}
