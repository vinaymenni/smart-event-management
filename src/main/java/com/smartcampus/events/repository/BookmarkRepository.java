package com.smartcampus.events.repository;

import com.smartcampus.events.model.Bookmark;
import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByStudentAndEvent(User student, Event event);

    void deleteByStudentAndEvent(User student, Event event);

    @Query("SELECT b FROM Bookmark b JOIN FETCH b.event WHERE b.student = :student ORDER BY b.createdAt DESC")
    List<Bookmark> findByStudentWithEvent(@Param("student") User student);
}

