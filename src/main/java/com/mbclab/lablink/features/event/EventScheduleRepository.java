package com.mbclab.lablink.features.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, String> {
    
    List<EventSchedule> findByEventId(String eventId);
    
    List<EventSchedule> findByActivityDateBetween(LocalDate start, LocalDate end);
    
    List<EventSchedule> findByActivityDate(LocalDate date);
    
    List<EventSchedule> findByActivityDateBetweenOrderByActivityDateAscStartTimeAsc(LocalDate start, LocalDate end);
    
    void deleteByEventId(String eventId);
}
