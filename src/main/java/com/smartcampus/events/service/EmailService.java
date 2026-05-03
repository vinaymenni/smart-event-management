package com.smartcampus.events.service;

import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.Registration;
import com.smartcampus.events.model.RegistrationTeamMember;
import com.smartcampus.events.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@smartcampus.edu}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void sendWelcomeEmail(User student) {
        String subject = "Welcome to Smart Campus Event Management System";
        String content = ""
                + "<div style='font-family:Inter,Arial,sans-serif;line-height:1.5'>"
                + "<h2>Welcome to Smart Campus Event Management System.</h2>"
                + "<p>Hi <strong>" + escape(student.getName()) + "</strong>,</p>"
                + "<p>Your account has been successfully created successfully.</p>"
                + "<p>You can now browse events, register, and download your participation certificate after completion.</p>"
                + "<br><p>Regards,<br><strong>Smart Campus Event Management System</strong></p>"
                + "</div>";
        sendEmail(student.getEmail(), subject, content);
    }

    /**
     * Sends a registration confirmation email.
     */
    public void sendRegistrationConfirmation(Registration registration) {
        User student = registration.getStudent();
        Event event = registration.getEvent();

        String subject = "Event Registration Confirmation";

        Map<String, String> details = new LinkedHashMap<>();
        details.put("Student Name", safe(student.getName()));
        details.put("Registered Email", safe(registration.getContactEmail()));
        details.put("Phone Number", safe(registration.getContactPhone()));
        details.put("Event Name", safe(event.getTitle()));
        details.put("Event Description", safe(event.getDescription()));
        details.put("Department", event.getDepartment() != null ? safe(event.getDepartment().name()) : "N/A");
        details.put("Event Type", safe(event.getEventType()));
        details.put("Venue", safe(event.getVenue()));
        details.put("Event Start Date", formatDate(event.getStartDate()));
        details.put("Event End Date", formatDate(event.getEndDate()));
        details.put("Last Date to Apply", formatDate(event.getLastDateToApply()));
        details.put("Event Timing", safe(event.getTiming())); // optional field (may be null)
        details.put("Registration ID / Ticket ID", safe(registration.getTicketCode()));
        details.put("Seat Number", "N/A");
        details.put("Registration Status", registration.getStatus() != null ? safe(registration.getStatus().name()) : "REGISTERED");
        details.put("Team Size", String.valueOf(registration.getTeamMembers() != null ? registration.getTeamMembers().size() : 1));

        String content = ""
                + "<div style='font-family:Inter,Arial,sans-serif;line-height:1.5'>"
                + "<h2>Event Registration Confirmation</h2>"
                + "<p>Hi <strong>" + escape(student.getName()) + "</strong>,</p>"
                + "<p>You have successfully registered for the event.</p>"
                + "<h3 style='margin-top:16px'>Event Details</h3>"
                + renderKeyValueTable(details)
                + renderTeamMembers(registration.getTeamMembers())
                + "<p style='margin-top:16px'>Thank you for registering.</p>"
                + "<br><p>Regards,<br><strong>Smart Campus Event Management System</strong></p>"
                + "</div>";

        String to = (registration.getContactEmail() != null && !registration.getContactEmail().isBlank())
                ? registration.getContactEmail()
                : student.getEmail();
        sendEmail(to, subject, content);
    }

    /**
     * Notify all registered students when an event is updated (venue/date/time/cancellation, etc.).
     */
    public void sendEventUpdateAlert(Event oldEvent, Event newEvent, List<Registration> activeRegistrations) {
        if (activeRegistrations == null || activeRegistrations.isEmpty()) {
            return;
        }

        String subject = "Event Updated: " + safe(newEvent.getTitle());

        Map<String, String> oldDetails = summarizeEvent(oldEvent);
        Map<String, String> newDetails = summarizeEvent(newEvent);
        String changeSummary = renderDiffList(oldDetails, newDetails);

        String baseContent = ""
                + "<div style='font-family:Inter,Arial,sans-serif;line-height:1.5'>"
                + "<h2>Event Update Alert</h2>"
                + "<p>The event <strong>" + escape(safe(newEvent.getTitle())) + "</strong> has been updated.</p>"
                + "<h3 style='margin-top:16px'>What changed</h3>"
                + changeSummary
                + "<h3 style='margin-top:16px'>Old Details</h3>"
                + renderKeyValueTable(oldDetails)
                + "<h3 style='margin-top:16px'>New Details</h3>"
                + renderKeyValueTable(newDetails)
                + "<br><p>Regards,<br><strong>Smart Campus Event Management System</strong></p>"
                + "</div>";

        for (Registration reg : activeRegistrations) {
            if (reg.getStudent() == null || reg.getStudent().getEmail() == null) continue;
            String to = reg.getStudent().getEmail();
            sendEmail(to, subject, baseContent);
        }
    }

    /**
     * Sends an upcoming event reminder.
     */
    public void sendEventReminder(User student, Event event, int daysLeft) {
        String subject = "Reminder: " + event.getTitle() + " is in " + daysLeft + " days!";
        String content = "<h3>Hello " + student.getName() + ",</h3>"
                + "<p>This is a quick reminder that <strong>" + event.getTitle() + "</strong> is coming up soon on "
                + event.getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + ".</p>"
                + "<p>Don't forget to mark your calendar!</p>"
                + "<br><p>See you there,<br>Smart Campus Event Management</p>";

        sendEmail(student.getEmail(), subject, content);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            // Attempt to send real email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indicates HTML format

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            // Fallback for demonstration since actual SMTP credentials might be missing
            log.warn("SMTP Connection failed or not configured. Printing email to console instead. reason={}", e.getMessage());
            log.info(
                    "\n========== SIMULATED EMAIL ==========\nTo: {}\nSubject: {}\nContent:\n{}\n=====================================",
                    to, subject, text);
        }
    }

    private static String renderTeamMembers(List<RegistrationTeamMember> teamMembers) {
        if (teamMembers == null || teamMembers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<h3 style='margin-top:16px'>Team Members</h3>");
        sb.append("<table style='width:100%;border-collapse:collapse'>");
        sb.append("<tr>")
                .append("<th style='text-align:left;padding:8px 10px;border:1px solid #e5e7eb;background:#f9fafb'>#</th>")
                .append("<th style='text-align:left;padding:8px 10px;border:1px solid #e5e7eb;background:#f9fafb'>Name</th>")
                .append("<th style='text-align:left;padding:8px 10px;border:1px solid #e5e7eb;background:#f9fafb'>Email</th>")
                .append("<th style='text-align:left;padding:8px 10px;border:1px solid #e5e7eb;background:#f9fafb'>Phone</th>")
                .append("</tr>");
        int idx = 1;
        for (RegistrationTeamMember m : teamMembers) {
            sb.append("<tr>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb'>").append(idx++).append("</td>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb'>").append(escape(safe(m.getName()))).append("</td>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb'>").append(escape(safe(m.getEmail()))).append("</td>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb'>").append(escape(safe(m.getPhone()))).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static Map<String, String> summarizeEvent(Event e) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Event Name", safe(e.getTitle()));
        details.put("Department", e.getDepartment() != null ? safe(e.getDepartment().name()) : "N/A");
        details.put("Event Type", safe(e.getEventType()));
        details.put("Venue", safe(e.getVenue()));
        details.put("Start Date", formatDate(e.getStartDate()));
        details.put("End Date", formatDate(e.getEndDate()));
        details.put("Last Date to Apply", formatDate(e.getLastDateToApply()));
        details.put("Timing", safe(e.getTiming()));
        return details;
    }

    private static String renderKeyValueTable(Map<String, String> details) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table style='width:100%;border-collapse:collapse'>");
        for (Map.Entry<String, String> entry : details.entrySet()) {
            sb.append("<tr>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb;background:#f9fafb;width:35%'><strong>")
                    .append(escape(entry.getKey()))
                    .append("</strong></td>")
                    .append("<td style='padding:8px 10px;border:1px solid #e5e7eb'>")
                    .append(escape(entry.getValue()))
                    .append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String renderDiffList(Map<String, String> oldDetails, Map<String, String> newDetails) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul style='margin:0;padding-left:18px'>");
        boolean any = false;
        for (String key : newDetails.keySet()) {
            String oldVal = oldDetails.get(key);
            String newVal = newDetails.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                any = true;
                sb.append("<li><strong>").append(escape(key)).append(":</strong> ")
                        .append("<span style='color:#6b7280'>").append(escape(safe(oldVal))).append("</span>")
                        .append(" &rarr; ")
                        .append("<span style='color:#111827'>").append(escape(safe(newVal))).append("</span>")
                        .append("</li>");
            }
        }
        if (!any) {
            sb.append("<li>No visible field changes detected.</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "N/A" : date.format(DATE_FMT);
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
