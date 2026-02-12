package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.event.dto.EventResponse;
import com.mbclab.lablink.shared.approval.AbstractApprovalService;
import com.mbclab.lablink.shared.approval.ApprovalRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Approval Service - menangani approval workflow untuk Event.
 * 
 * Extends AbstractApprovalService untuk reuse logic approve/reject/getPending.
 * HRD dan ADMIN dapat approve event.
 */
@Service
@Transactional
public class EventApprovalService extends AbstractApprovalService<Event, EventResponse> {
    
    private final EventRepository eventRepository;
    private final EventCommitteeRepository committeeRepository;
    
    public EventApprovalService(
            ApplicationEventPublisher eventPublisher,
            EventRepository eventRepository,
            EventCommitteeRepository committeeRepository) {
        super(eventPublisher);
        this.eventRepository = eventRepository;
        this.committeeRepository = committeeRepository;
    }
    
    @Override
    protected ApprovalRepository<Event, String> getRepository() {
        return eventRepository;
    }
    
    @Override
    protected String getEntityType() {
        return "EVENT";
    }
    
    @Override
    protected String getNotFoundMessage() {
        return "Event tidak ditemukan";
    }
    
    @Override
    protected EventResponse toResponse(Event event) {
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
                .approvalStatus(event.getApprovalStatus())
                .rejectionReason(event.getRejectionReason())
                .approvedAt(event.getApprovedAt())
                .approvedBy(event.getApprovedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .pic(picSummary)
                .committee(committeeList)
                .build();
    }
    
    // No custom verifyApprovalPermission needed - 
    // controller-level @PreAuthorize handles ADMIN/HRD check
}
