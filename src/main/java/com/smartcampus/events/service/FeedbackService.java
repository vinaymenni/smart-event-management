package com.smartcampus.events.service;

import com.smartcampus.events.dto.FeedbackDto;
import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.Feedback;
import com.smartcampus.events.model.User;
import com.smartcampus.events.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Feedback submitFeedback(User student, Event event, FeedbackDto dto) {
        if (feedbackRepository.existsByStudentAndEvent(student, event)) {
            throw new IllegalStateException("You have already submitted feedback for this event.");
        }
        Feedback feedback = Feedback.builder()
                .student(student)
                .event(event)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> findByEvent(Event event) {
        return feedbackRepository.findByEvent(event);
    }

    public boolean hasSubmittedFeedback(User student, Event event) {
        return feedbackRepository.existsByStudentAndEvent(student, event);
    }

    public Double averageRating(Event event) {
        return feedbackRepository.averageRatingByEvent(event);
    }
}
