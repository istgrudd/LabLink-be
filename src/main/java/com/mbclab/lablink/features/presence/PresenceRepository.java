package com.mbclab.lablink.features.presence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, String> {
    
    // For User History
    List<Presence> findByMemberIdOrderByDateDesc(String memberId);

    // For Admin Recap / Filters
    List<Presence> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Presence> findByTypeAndDateBetween(Presence.PresenceType type, LocalDate startDate, LocalDate endDate);
    
    // For period filtering
    List<Presence> findByPeriodId(String periodId);
}
