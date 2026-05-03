package com.smartcampus.events.controller;

import com.smartcampus.events.dto.EventDto;
import com.smartcampus.events.model.*;
import com.smartcampus.events.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final AnnouncementService announcementService;

    private User getCurrentAdmin(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    /* ===== DASHBOARD ===== */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getCurrentAdmin(userDetails);

        long totalEvents = eventService.countAllEvents();
        long totalRegistrations = registrationService.countTotalRegistrations();
        List<Announcement> announcements = announcementService.findRecent();
        Page<Event> recentEvents = eventService.findAllPaged(0, 5);

        model.addAttribute("admin", admin);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalRegistrations", totalRegistrations);
        model.addAttribute("announcements", announcements);
        model.addAttribute("recentEvents", recentEvents.getContent());
        return "admin/dashboard";
    }

    /* ===== EVENT MANAGEMENT ===== */
    @GetMapping("/events")
    public String manageEvents(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
        model.addAttribute("admin", getCurrentAdmin(userDetails));
        model.addAttribute("events", events);
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("eventType", eventType);
        model.addAttribute("departments", Department.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());
        return "admin/events";
    }

    @GetMapping("/events/new")
    public String newEventForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("admin", getCurrentAdmin(userDetails));
        model.addAttribute("eventDto", new EventDto());
        model.addAttribute("departments", Department.values());
        model.addAttribute("isEdit", false);
        return "admin/event-form";
    }

    @PostMapping("/events/new")
    public String createEvent(@Valid @ModelAttribute("eventDto") EventDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("admin", getCurrentAdmin(userDetails));
            model.addAttribute("departments", Department.values());
            model.addAttribute("isEdit", false);
            return "admin/event-form";
        }
        if (dto.getMinTeamSize() != null && dto.getMaxTeamSize() != null && dto.getMinTeamSize() > dto.getMaxTeamSize()) {
            model.addAttribute("admin", getCurrentAdmin(userDetails));
            model.addAttribute("departments", Department.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMsg", "Minimum team size cannot be greater than maximum team size.");
            return "admin/event-form";
        }
        User admin = getCurrentAdmin(userDetails);
        eventService.createEvent(dto, admin);
        redirectAttrs.addFlashAttribute("successMsg", "Event created successfully!");
        return "redirect:/admin/events";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        Event event = eventService.findById(id);
        model.addAttribute("admin", getCurrentAdmin(userDetails));
        model.addAttribute("eventDto", eventService.toDto(event));
        model.addAttribute("eventId", id);
        model.addAttribute("departments", Department.values());
        model.addAttribute("isEdit", true);
        return "admin/event-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
            @Valid @ModelAttribute("eventDto") EventDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("admin", getCurrentAdmin(userDetails));
            model.addAttribute("departments", Department.values());
            model.addAttribute("eventId", id);
            model.addAttribute("isEdit", true);
            return "admin/event-form";
        }
        if (dto.getMinTeamSize() != null && dto.getMaxTeamSize() != null && dto.getMinTeamSize() > dto.getMaxTeamSize()) {
            model.addAttribute("admin", getCurrentAdmin(userDetails));
            model.addAttribute("departments", Department.values());
            model.addAttribute("eventId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMsg", "Minimum team size cannot be greater than maximum team size.");
            return "admin/event-form";
        }
        eventService.updateEvent(id, dto);
        redirectAttrs.addFlashAttribute("successMsg", "Event updated successfully!");
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        eventService.deleteEvent(id);
        redirectAttrs.addFlashAttribute("successMsg", "Event deleted successfully!");
        return "redirect:/admin/events";
    }

    /* ===== REGISTRATIONS ===== */
    @GetMapping("/registrations")
    public String viewRegistrations(@RequestParam(required = false) Long eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        model.addAttribute("admin", getCurrentAdmin(userDetails));

        if (eventId != null) {
            Event event = eventService.findById(eventId);
            List<Registration> registrations = registrationService.findByEvent(event);
            model.addAttribute("selectedEvent", event);
            model.addAttribute("registrations", registrations);
        }

        model.addAttribute("events", eventService.findAll());
        return "admin/registrations";
    }

    /* ===== ANALYTICS ===== */
    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = getCurrentAdmin(userDetails);

        long totalEvents = eventService.countAllEvents();
        long totalRegistrations = registrationService.countTotalRegistrations();

        List<Object[]> regByDept = registrationService.countRegistrationsByDepartment();
        List<Object[]> eventsByDept = eventService.countEventsByDepartment();
        List<Object[]> eventsByType = eventService.countEventsByType();

        // Build chart-friendly maps
        Map<String, Long> regByDeptMap = new HashMap<>();
        for (Object[] row : regByDept) {
            regByDeptMap.put(row[0].toString(), (Long) row[1]);
        }
        Map<String, Long> eventsByDeptMap = new HashMap<>();
        for (Object[] row : eventsByDept) {
            eventsByDeptMap.put(row[0].toString(), (Long) row[1]);
        }
        Map<String, Long> eventsByTypeMap = new HashMap<>();
        for (Object[] row : eventsByType) {
            eventsByTypeMap.put(row[0].toString(), (Long) row[1]);
        }

        model.addAttribute("admin", admin);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalRegistrations", totalRegistrations);
        model.addAttribute("regByDeptMap", regByDeptMap);
        model.addAttribute("eventsByDeptMap", eventsByDeptMap);
        model.addAttribute("eventsByTypeMap", eventsByTypeMap);
        model.addAttribute("departments", Department.values());
        return "admin/analytics";
    }

    /* ===== ANNOUNCEMENTS ===== */
    @GetMapping("/announcements")
    public String announcements(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("admin", getCurrentAdmin(userDetails));
        model.addAttribute("announcements", announcementService.findAll());
        model.addAttribute("events", eventService.findAll());
        return "admin/announcements";
    }

    @PostMapping("/announcements")
    public String postAnnouncement(@RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) Long eventId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttrs) {
        User admin = getCurrentAdmin(userDetails);
        Event event = eventId != null ? eventService.findById(eventId) : null;
        announcementService.createAnnouncement(title, message, admin, event);
        redirectAttrs.addFlashAttribute("successMsg", "Announcement posted successfully!");
        return "redirect:/admin/announcements";
    }
}
