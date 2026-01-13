package com.mbclab.lablink.features.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommitteeRepository extends JpaRepository<EventCommittee, EventCommitteeId> {
    List<EventCommittee> findByEventId(String eventId);
    void deleteByEventIdAndMemberId(String eventId, String memberId);
}
