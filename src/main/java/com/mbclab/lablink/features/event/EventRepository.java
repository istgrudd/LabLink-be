package com.mbclab.lablink.features.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    Optional<Event> findByEventCode(String eventCode);
    long countByEventCodeStartingWith(String prefix);
}
