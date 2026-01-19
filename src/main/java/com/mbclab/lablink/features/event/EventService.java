package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.event.dto.*;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventCommitteeRepository committeeRepository;
    private final EventScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final EventCodeGenerator eventCodeGenerator;
    private final AcademicPeriodRepository periodRepository;
    private final com.mbclab.lablink.features.archive.ArchiveRepository archiveRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CREATE ==========
    
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        // 1. Get PIC
        ResearchAssistant pic = memberRepository.findById(request.getPicId())
                .orElseThrow(() -> new RuntimeException("PIC tidak ditemukan"));
        
        // 2. Generate code
        String eventCode = eventCodeGenerator.generate();
        
        // 3. Create event
        Event event = new Event();
        event.setEventCode(eventCode);
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setStatus("PLANNED");
        event.setPic(pic);
        
        // 4. Auto-assign to active period
        periodRepository.findByIsActiveTrue().ifPresent(event::setPeriod);
        
        Event saved = eventRepository.save(event);
        
        // 5. Create Schedules if present
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            for (EventScheduleRequest scheduleReq : request.getSchedules()) {
                // Validate date
                if (scheduleReq.getActivityDate().isBefore(saved.getStartDate()) || 
                    scheduleReq.getActivityDate().isAfter(saved.getEndDate())) {
                    throw new RuntimeException("Tanggal jadwal " + scheduleReq.getActivityDate() + 
                            " diluar rentang event (" + saved.getStartDate() + " - " + saved.getEndDate() + ")");
                }
                
                EventSchedule schedule = new EventSchedule();
                schedule.setEvent(saved);
                schedule.setActivityDate(scheduleReq.getActivityDate());
                schedule.setTitle(scheduleReq.getTitle());
                schedule.setDescription(scheduleReq.getDescription());
                schedule.setStartTime(scheduleReq.getStartTime());
                schedule.setEndTime(scheduleReq.getEndTime());
                schedule.setLocation(scheduleReq.getLocation());
                
                scheduleRepository.save(schedule);
            }
        }
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.create(
                "EVENT", saved.getId(), saved.getName(),
                "Created event: " + saved.getEventCode()));
        
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getEventsByPeriod(String periodId) {
        return eventRepository.findByPeriodId(periodId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getOrphanEvents() {
        return eventRepository.findByPeriodIsNull().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        return toResponse(event);
    }

    public EventResponse getEventByCode(String eventCode) {
        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        return toResponse(event);
    }

    // ========== UPDATE ==========
    
    @Transactional
    public EventResponse updateEvent(String id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        
        // Partial update
        if (request.getName() != null && !request.getName().isBlank()) {
            event.setName(request.getName());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            event.setStatus(request.getStatus().toUpperCase());
        }
        // Update PIC
        if (request.getPicId() != null && !request.getPicId().isBlank()) {
            ResearchAssistant pic = memberRepository.findById(request.getPicId())
                    .orElseThrow(() -> new RuntimeException("PIC tidak ditemukan"));
            event.setPic(pic);
        }

        // Update Committee (Replace All)
        if (request.getCommittee() != null) {
            // Clear existing
            event.getCommittee().clear();
            
            // Add new
            for (UpdateEventRequest.CommitteeMemberRequest cmd : request.getCommittee()) {
                ResearchAssistant member = memberRepository.findById(cmd.getMemberId())
                        .orElseThrow(() -> new RuntimeException("Member " + cmd.getMemberId() + " tidak ditemukan"));
                
                EventCommittee committee = new EventCommittee(event, member, cmd.getRole());
                event.getCommittee().add(committee);
            }
        }
        
        Event saved = eventRepository.save(event);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "EVENT", saved.getId(), saved.getName(),
                "Updated event: " + saved.getEventCode()));
        
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional
    public void deleteEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        String eventName = event.getName();
        String eventCode = event.getEventCode();
        
        // Check for archives
        if (!archiveRepository.findByEventId(id).isEmpty()) {
            throw new RuntimeException("Gagal menghapus: Event ini memiliki arsip (laporan/sertifikat) yang terhubung. Hapus arsip terkait terlebih dahulu.");
        }
        
        // Manually clear committee to ensure orphan removal works
        event.getCommittee().clear();
        eventRepository.saveAndFlush(event);
        
        // Delete event
        eventRepository.delete(event);
        eventRepository.flush(); // Force commit
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.delete(
                "EVENT", id, eventName,
                "Deleted event: " + eventCode));
    }

    // ========== COMMITTEE MANAGEMENT ==========
    
    @Transactional
    public EventResponse addCommitteeMember(String eventId, AddCommitteeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        
        ResearchAssistant member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        // Check if already in committee
        EventCommitteeId compositeId = new EventCommitteeId(eventId, request.getMemberId());
        if (committeeRepository.existsById(compositeId)) {
            throw new RuntimeException("Member sudah terdaftar sebagai panitia");
        }
        
        EventCommittee committee = new EventCommittee(event, member, request.getRole());
        committeeRepository.save(committee);
        
        // Refresh event to get updated committee list
        event = eventRepository.findById(eventId).orElseThrow();
        return toResponse(event);
    }

    public List<EventResponse.CommitteeMember> getCommitteeMembers(String eventId) {
        List<EventCommittee> committees = committeeRepository.findByEventId(eventId);
        return committees.stream()
                .map(c -> EventResponse.CommitteeMember.builder()
                        .memberId(c.getMember().getId())
                        .username(c.getMember().getUsername())
                        .fullName(c.getMember().getFullName())
                        .role(c.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateCommitteeRole(String eventId, String memberId, UpdateCommitteeRoleRequest request) {
        EventCommitteeId compositeId = new EventCommitteeId(eventId, memberId);
        EventCommittee committee = committeeRepository.findById(compositeId)
                .orElseThrow(() -> new RuntimeException("Panitia tidak ditemukan"));
        
        committee.setRole(request.getRole());
        committeeRepository.save(committee);
        
        Event event = eventRepository.findById(eventId).orElseThrow();
        return toResponse(event);
    }

    @Transactional
    public EventResponse removeCommitteeMember(String eventId, String memberId) {
        EventCommitteeId compositeId = new EventCommitteeId(eventId, memberId);
        if (!committeeRepository.existsById(compositeId)) {
            throw new RuntimeException("Panitia tidak ditemukan");
        }
        committeeRepository.deleteById(compositeId);
        
        Event event = eventRepository.findById(eventId).orElseThrow();
        return toResponse(event);
    }

    // ========== SCHEDULE MANAGEMENT (CALENDAR) ==========
    
    @Transactional
    public EventScheduleResponse addSchedule(String eventId, EventScheduleRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
        
        // Validate date is within event range
        if (request.getActivityDate().isBefore(event.getStartDate()) || 
            request.getActivityDate().isAfter(event.getEndDate())) {
            throw new RuntimeException("Tanggal kegiatan harus dalam rentang event (" + 
                    event.getStartDate() + " - " + event.getEndDate() + ")");
        }
        
        EventSchedule schedule = new EventSchedule();
        schedule.setEvent(event);
        schedule.setActivityDate(request.getActivityDate());
        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLocation(request.getLocation());
        
        EventSchedule saved = scheduleRepository.save(schedule);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.create(
                "EVENT_SCHEDULE", saved.getId(), saved.getTitle(),
                "Added schedule to event: " + event.getEventCode() + " on " + saved.getActivityDate()));
        
        return toScheduleResponse(saved);
    }
    
    @Transactional
    public EventScheduleResponse updateSchedule(String scheduleId, EventScheduleRequest request) {
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule tidak ditemukan"));
        
        Event event = schedule.getEvent();
        
        // Validate date is within event range
        if (request.getActivityDate() != null && 
            (request.getActivityDate().isBefore(event.getStartDate()) || 
             request.getActivityDate().isAfter(event.getEndDate()))) {
            throw new RuntimeException("Tanggal kegiatan harus dalam rentang event (" + 
                    event.getStartDate() + " - " + event.getEndDate() + ")");
        }
        
        if (request.getActivityDate() != null) {
            schedule.setActivityDate(request.getActivityDate());
        }
        if (request.getTitle() != null) {
            schedule.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            schedule.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            schedule.setLocation(request.getLocation());
        }
        
        EventSchedule saved = scheduleRepository.save(schedule);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "EVENT_SCHEDULE", saved.getId(), saved.getTitle(),
                "Updated schedule on " + saved.getActivityDate()));
        
        return toScheduleResponse(saved);
    }
    
    @Transactional
    public void deleteSchedule(String scheduleId) {
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule tidak ditemukan"));
        
        String title = schedule.getTitle();
        String eventCode = schedule.getEvent().getEventCode();
        
        scheduleRepository.delete(schedule);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.delete(
                "EVENT_SCHEDULE", scheduleId, title,
                "Deleted schedule from event: " + eventCode));
    }
    
    public java.util.List<EventScheduleResponse> getSchedulesByEvent(String eventId) {
        return scheduleRepository.findByEventId(eventId).stream()
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());
    }
    
    public java.util.List<EventScheduleResponse> getCalendarSchedules(java.time.LocalDate start, java.time.LocalDate end) {
        return scheduleRepository.findByActivityDateBetweenOrderByActivityDateAscStartTimeAsc(start, end).stream()
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());
    }

    // ========== HELPER: Convert to Response DTO ==========
    
    private EventScheduleResponse toScheduleResponse(EventSchedule schedule) {
        return EventScheduleResponse.builder()
                .id(schedule.getId())
                .eventId(schedule.getEvent().getId())
                .eventCode(schedule.getEvent().getEventCode())
                .eventName(schedule.getEvent().getName())
                .activityDate(schedule.getActivityDate())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
    
    private EventResponse toResponse(Event event) {
        // PIC summary
        EventResponse.MemberSummary picSummary = null;
        if (event.getPic() != null) {
            picSummary = EventResponse.MemberSummary.builder()
                    .id(event.getPic().getId())
                    .username(event.getPic().getUsername())
                    .fullName(event.getPic().getFullName())
                    .expertDivision(event.getPic().getExpertDivision())
                    .build();
        }
        
        // Committee list - use repository to avoid lazy loading issues
        List<EventCommittee> committees = committeeRepository.findByEventId(event.getId());
        List<EventResponse.CommitteeMember> committeeList = committees.stream()
                .map(c -> EventResponse.CommitteeMember.builder()
                        .memberId(c.getMember().getId())
                        .username(c.getMember().getUsername())
                        .fullName(c.getMember().getFullName())
                        .role(c.getRole())
                        .build())
                .collect(Collectors.toList());
        
        return EventResponse.builder()
                .id(event.getId())
                .eventCode(event.getEventCode())
                .name(event.getName())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .pic(picSummary)
                .committee(committeeList)
                .build();
    }
}
