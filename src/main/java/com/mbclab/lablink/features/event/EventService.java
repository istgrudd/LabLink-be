package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.event.dto.*;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import lombok.RequiredArgsConstructor;
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
    private final MemberRepository memberRepository;
    private final EventCodeGenerator eventCodeGenerator;

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
        
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
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
        if (request.getPicId() != null && !request.getPicId().isBlank()) {
            ResearchAssistant pic = memberRepository.findById(request.getPicId())
                    .orElseThrow(() -> new RuntimeException("PIC tidak ditemukan"));
            event.setPic(pic);
        }
        
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional
    public void deleteEvent(String id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event tidak ditemukan");
        }
        eventRepository.deleteById(id);
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

    // ========== HELPER: Convert to Response DTO ==========
    
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
