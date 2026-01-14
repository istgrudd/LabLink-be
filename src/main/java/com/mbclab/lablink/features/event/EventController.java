package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.event.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        EventResponse created = eventService.createEvent(request);
        return ResponseEntity.ok(created);
    }

    // ========== READ ==========
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) String periodId) {
        if (periodId != null && !periodId.isBlank()) {
            return ResponseEntity.ok(eventService.getEventsByPeriod(periodId));
        }
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/code/{eventCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventByCode(@PathVariable String eventCode) {
        return ResponseEntity.ok(eventService.getEventByCode(eventCode));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable String id,
            @RequestBody UpdateEventRequest request) {
        EventResponse updated = eventService.updateEvent(id, request);
        return ResponseEntity.ok(updated);
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ========== COMMITTEE MANAGEMENT ==========
    
    @PostMapping("/{id}/committee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> addCommitteeMember(
            @PathVariable String id,
            @RequestBody AddCommitteeRequest request) {
        EventResponse updated = eventService.addCommitteeMember(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/committee")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse.CommitteeMember>> getCommitteeMembers(@PathVariable String id) {
        return ResponseEntity.ok(eventService.getCommitteeMembers(id));
    }

    @PutMapping("/{id}/committee/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateCommitteeRole(
            @PathVariable String id,
            @PathVariable String memberId,
            @RequestBody UpdateCommitteeRoleRequest request) {
        EventResponse updated = eventService.updateCommitteeRole(id, memberId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/committee/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> removeCommitteeMember(
            @PathVariable String id,
            @PathVariable String memberId) {
        EventResponse updated = eventService.removeCommitteeMember(id, memberId);
        return ResponseEntity.ok(updated);
    }
}
