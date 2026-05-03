package com.smartcampus.events.repository;

import com.smartcampus.events.model.Department;
import com.smartcampus.events.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        List<Event> findByDepartment(Department department);

        List<Event> findByEventType(String eventType);

        List<Event> findByStartDate(LocalDate date);

        @Query("SELECT e FROM Event e WHERE e.startDate >= :from AND e.startDate <= :to ORDER BY e.startDate ASC")
        List<Event> findUpcomingBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

        Page<Event> findAll(Pageable pageable);

        @Query("SELECT e FROM Event e WHERE " +
                        "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:department IS NULL OR e.department = :department) AND " +
                        "(:eventType IS NULL OR e.eventType = :eventType) AND " +
                        "(:startDate IS NULL OR e.startDate >= :startDate) AND " +
                        "(:endDate IS NULL OR e.endDate <= :endDate)")
        Page<Event> searchEvents(@Param("keyword") String keyword,
                        @Param("department") Department department,
                        @Param("eventType") String eventType,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        @Query("SELECT COUNT(e) FROM Event e")
        long countAllEvents();

        @Query("SELECT e.department, COUNT(e) FROM Event e GROUP BY e.department")
        List<Object[]> countEventsByDepartment();

        @Query("SELECT e.eventType, COUNT(e) FROM Event e GROUP BY e.eventType")
        List<Object[]> countEventsByType();
}
