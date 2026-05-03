package com.smartcampus.events.repository;

import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.Feedback;
import com.smartcampus.events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByEvent(Event event);

    List<Feedback> findByStudent(User student);

    Optional<Feedback> findByStudentAndEvent(User student, Event event);

    boolean existsByStudentAndEvent(User student, Event event);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event = :event")
    Double averageRatingByEvent(@Param("event") Event event);
}
