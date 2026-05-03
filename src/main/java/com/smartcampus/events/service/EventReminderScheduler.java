package com.smartcampus.events.service;

import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.Registration;
import com.smartcampus.events.repository.EventRepository;
import com.smartcampus.events.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventReminderScheduler {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EmailService emailService;

    /**
     * Cron Job scheduled to run daily at 8:00 AM server time.
     * Checks for events starting exactly 3 days from today and emails all
     * registered participants.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendUpcomingEventReminders() {
        log.info("Running daily Scheduled Job: Event Reminder Checks");

        LocalDate targetDate = LocalDate.now().plusDays(3);
        List<Event> upcomingEvents = eventRepository.findByStartDate(targetDate);

        if (upcomingEvents.isEmpty()) {
            log.info("No events scheduled exactly 3 days from now (on {}).", targetDate);
            return;
        }

        for (Event event : upcomingEvents) {
            if ("COMPLETED".equals(event.getStatus().name()) || "CANCELLED".equals(event.getStatus().name())) {
                continue;
            }

            log.info("Sending reminders for upcoming event: {}", event.getTitle());
            List<Registration> registrations = registrationRepository.findByEvent(event);

            int emailsSent = 0;
            for (Registration reg : registrations) {
                if ("REGISTERED".equals(reg.getStatus().name())) {
                    emailService.sendEventReminder(reg.getStudent(), event, 3);
                    emailsSent++;
                }
            }
            log.info("Successfully sent {} reminder emails for event {}.", emailsSent, event.getTitle());
        }
    }
}
