package com.smartcampus.events.service;

import com.smartcampus.events.model.Announcement;
import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.User;
import com.smartcampus.events.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional
    public Announcement createAnnouncement(String title, String message, User admin, Event event) {
        Announcement ann = Announcement.builder()
                .title(title)
                .message(message)
                .postedBy(admin)
                .event(event)
                .build();
        return announcementRepository.save(ann);
    }

    public List<Announcement> findAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Announcement> findRecent() {
        return announcementRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
