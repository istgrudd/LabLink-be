package com.mbclab.lablink.features.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    Optional<Event> findByEventCode(String eventCode);
    List<Event> findByPeriodId(String periodId);
    long countByEventCodeStartingWith(String prefix);
    int countByPeriodId(String periodId);
    
    // For cascade delete
    // For cascade delete
    void deleteByPeriodId(String periodId);

    // For orphan filter
    List<Event> findByPeriodIsNull();
}
