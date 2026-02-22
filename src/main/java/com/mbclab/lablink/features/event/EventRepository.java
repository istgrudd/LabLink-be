package com.mbclab.lablink.features.event;

import com.mbclab.lablink.shared.approval.ApprovalRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends ApprovalRepository<Event, String> {
    Optional<Event> findByEventCode(String eventCode);

    long countByEventCodeStartingWith(String prefix);
    
    // For approval workflow
    List<Event> findByApprovalStatus(String approvalStatus);
}
