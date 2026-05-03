package com.smartcampus.events.controller;

import com.smartcampus.events.dto.FeedbackDto;
import com.smartcampus.events.exception.DuplicateRegistrationException;
import com.smartcampus.events.model.*;
import com.smartcampus.events.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final FeedbackService feedbackService;
    private final AnnouncementService announcementService;
    private final BookmarkService bookmarkService;
    private final ParticipationCertificateService participationCertificateService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = getCurrentUser(userDetails);
        Page<Event> events = eventService.findAllPaged(0, 6);
        List<Registration> myRegistrations = registrationService.findByStudentActive(student);
        List<Announcement> announcements = announcementService.findRecent();

        model.addAttribute("student", student);
        model.addAttribute("events", events.getContent());
        model.addAttribute("myRegistrations", myRegistrations);
        model.addAttribute("announcements", announcements);
        model.addAttribute("totalEvents", eventService.countAllEvents());
        model.addAttribute("myRegCount", myRegistrations.size());
        return "student/dashboard";
    }

    @GetMapping("/events")
    public String browseEvents(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String eventType,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        Department dept = null;
        if (department != null && !department.isEmpty()) {
            try {
                dept = Department.valueOf(department);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Event> events = eventService.searchEvents(keyword, dept, eventType, null, null, page, size);
        User student = getCurrentUser(userDetails);
        Set<Long> bookmarkedIds = bookmarkService.listBookmarks(student).stream()
                .map(b -> b.getEvent().getId())
                .collect(Collectors.toSet());
        model.addAttribute("student", student);
        model.addAttribute("events", events);
        model.addAttribute("bookmarkedIds", bookmarkedIds);
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("eventType", eventType);
        model.addAttribute("departments", Department.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());
        return "student/events";
    }

    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = getCurrentUser(userDetails);
        Event event = eventService.findById(id);
        boolean alreadyRegistered = registrationService.isStudentRegistered(student, event);
        boolean feedbackGiven = feedbackService.hasSubmittedFeedback(student, event);
        List<Feedback> feedbacks = feedbackService.findByEvent(event);
        Double avgRating = feedbackService.averageRating(event);
        boolean bookmarked = bookmarkService.isBookmarked(student, event);

        model.addAttribute("student", student);
        model.addAttribute("event", event);
        model.addAttribute("alreadyRegistered", alreadyRegistered);
        model.addAttribute("feedbackGiven", feedbackGiven);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("feedbackDto", new FeedbackDto());
        model.addAttribute("bookmarked", bookmarked);
        return "student/event-detail";
    }

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) List<String> memberName,
            @RequestParam(required = false) List<String> memberEmail,
            @RequestParam(required = false) List<String> memberPhone,
            RedirectAttributes redirectAttrs) {
        User student = getCurrentUser(userDetails);
        try {
            Registration reg = registrationService.registerForEvent(student, id, email, phone, memberName, memberEmail,
                    memberPhone);
            redirectAttrs.addFlashAttribute("successMsg",
                    "Registered successfully! Your ticket code: " + reg.getTicketCode());
        } catch (DuplicateRegistrationException ex) {
            redirectAttrs.addFlashAttribute("errorMsg", "You are already registered for this event.");
        } catch (IllegalArgumentException ex) {
            redirectAttrs.addFlashAttribute("errorMsg", ex.getMessage());
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "Registration could not be completed due to concurrent request. Please try once.");
        }
        return "redirect:/student/events/" + id;
    }

    @PostMapping("/events/{id}/bookmark")
    public String bookmarkEvent(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs,
            @RequestHeader(value = "Referer", required = false) String referer) {
        User student = getCurrentUser(userDetails);
        Event event = eventService.findById(id);
        bookmarkService.addBookmark(student, event);
        redirectAttrs.addFlashAttribute("successMsg", "Event saved successfully.");
        return "redirect:" + (referer != null ? referer : ("/student/events/" + id));
    }

    @PostMapping("/events/{id}/unbookmark")
    public String unbookmarkEvent(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs,
            @RequestHeader(value = "Referer", required = false) String referer) {
        User student = getCurrentUser(userDetails);
        Event event = eventService.findById(id);
        bookmarkService.removeBookmark(student, event);
        redirectAttrs.addFlashAttribute("successMsg", "Event removed from saved list.");
        return "redirect:" + (referer != null ? referer : ("/student/events/" + id));
    }

    @GetMapping("/saved-events")
    public String savedEvents(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = getCurrentUser(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("bookmarks", bookmarkService.listBookmarks(student));
        return "student/saved-events";
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User student = getCurrentUser(userDetails);
        LocalDate today = LocalDate.now();
        int y = (year == null) ? today.getYear() : year;
        int m = (month == null) ? today.getMonthValue() : month;

        YearMonth ym = YearMonth.of(y, m);
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();

        List<Event> monthEvents = eventService.findUpcomingBetween(first, last);
        Map<String, List<Event>> eventsByDate = new HashMap<>();
        for (Event e : monthEvents) {
            String key = e.getStartDate() != null ? e.getStartDate().toString() : "";
            eventsByDate.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(e);
        }

        int shift = (first.getDayOfWeek().getValue() % 7); // Sunday = 0
        LocalDate gridStart = first.minusDays(shift);
        List<List<LocalDate>> weeks = new java.util.ArrayList<>();
        LocalDate cursor = gridStart;
        for (int w = 0; w < 6; w++) {
            List<LocalDate> week = new java.util.ArrayList<>();
            for (int d = 0; d < 7; d++) {
                week.add(cursor);
                cursor = cursor.plusDays(1);
            }
            weeks.add(week);
        }

        model.addAttribute("student", student);
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        model.addAttribute("monthName", ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("weeks", weeks);
        model.addAttribute("eventsByDate", eventsByDate);
        model.addAttribute("today", today);
        model.addAttribute("firstOfMonth", first);
        model.addAttribute("lastOfMonth", last);
        model.addAttribute("dow", DayOfWeek.values());
        return "student/calendar";
    }

    @GetMapping("/my-registrations")
    public String myRegistrations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = getCurrentUser(userDetails);
        List<Registration> registrations = registrationService.findByStudent(student);
        model.addAttribute("student", student);
        model.addAttribute("registrations", registrations);
        return "student/my-registrations";
    }

    @GetMapping("/registrations/{regId}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long regId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User student = getCurrentUser(userDetails);
        Registration reg = registrationService.findByIdWithDetails(regId);

        if (!reg.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(403).build();
        }
        if (reg.getStatus() != RegistrationStatus.REGISTERED) {
            return ResponseEntity.badRequest().build();
        }
        if (reg.getEvent() == null || reg.getEvent().getStatus() != EventStatus.COMPLETED) {
            return ResponseEntity.badRequest().build();
        }

        ParticipationCertificate cert = participationCertificateService.issueIfAbsent(reg);
        byte[] pdf = participationCertificateService.generatePdf(cert);
        String filename = "certificate-" + cert.getCertificateCode() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @PostMapping("/registrations/{regId}/cancel")
    public String cancelRegistration(@PathVariable Long regId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs) {
        User student = getCurrentUser(userDetails);
        registrationService.cancelRegistration(student, regId);
        redirectAttrs.addFlashAttribute("successMsg", "Registration cancelled successfully.");
        return "redirect:/student/my-registrations";
    }

    @PostMapping("/events/{id}/feedback")
    public String submitFeedback(@PathVariable Long id,
            @Valid @ModelAttribute("feedbackDto") FeedbackDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs,
            Model model) {
        User student = getCurrentUser(userDetails);
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("feedbackError", "Please provide a valid rating (1-5).");
            return "redirect:/student/events/" + id;
        }
        Event event = eventService.findById(id);
        feedbackService.submitFeedback(student, event, dto);
        redirectAttrs.addFlashAttribute("successMsg", "Feedback submitted successfully. Thank you!");
        return "redirect:/student/events/" + id;
    }
}
