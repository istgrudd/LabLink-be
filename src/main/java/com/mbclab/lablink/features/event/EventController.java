package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.event.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    // ========== SCHEDULE MANAGEMENT (CALENDAR) ==========
    
    @PostMapping("/{eventId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventScheduleResponse> addSchedule(
            @PathVariable String eventId,
            @RequestBody EventScheduleRequest request) {
        EventScheduleResponse created = eventService.addSchedule(eventId, request);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{eventId}/schedules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventScheduleResponse>> getSchedulesByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(eventService.getSchedulesByEvent(eventId));
    }
    
    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventScheduleResponse> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody EventScheduleRequest request) {
        EventScheduleResponse updated = eventService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable String scheduleId) {
        eventService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/calendar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventScheduleResponse>> getCalendarSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(eventService.getCalendarSchedules(start, end));
    }
}
