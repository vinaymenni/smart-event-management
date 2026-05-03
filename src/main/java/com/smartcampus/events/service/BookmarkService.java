package com.smartcampus.events.service;

import com.smartcampus.events.model.Bookmark;
import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.User;
import com.smartcampus.events.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public boolean isBookmarked(User student, Event event) {
        return bookmarkRepository.existsByStudentAndEvent(student, event);
    }

    @Transactional
    public void addBookmark(User student, Event event) {
        if (bookmarkRepository.existsByStudentAndEvent(student, event)) return;
        Bookmark b = Bookmark.builder().student(student).event(event).build();
        bookmarkRepository.save(b);
    }

    @Transactional
    public void removeBookmark(User student, Event event) {
        bookmarkRepository.deleteByStudentAndEvent(student, event);
    }

    public List<Bookmark> listBookmarks(User student) {
        return bookmarkRepository.findByStudentWithEvent(student);
    }
}

