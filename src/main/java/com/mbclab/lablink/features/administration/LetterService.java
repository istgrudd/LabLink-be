package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.administration.dto.*;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository letterRepository;
    private final IncomingLetterRepository incomingLetterRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final LetterNumberGenerator letterNumberGenerator;
    private final AcademicPeriodRepository periodRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== SURAT KELUAR ====================
    
    @Transactional
    public LetterResponse createLetter(CreateLetterRequest request) {
        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Try to find requester in member table, but allow non-members (like admin) to also create letters
        ResearchAssistant requester = memberRepository.findByUsername(username).orElse(null);
        
        Letter letter = new Letter();
        // letterNumber will be generated on approval
        letter.setLetterType(request.getLetterType().toUpperCase());
        letter.setCategory(request.getCategory().toUpperCase());
        letter.setSubject(request.getSubject());
        letter.setRecipient(request.getRecipient());
        letter.setContent(request.getContent());
        letter.setAttachment(request.getAttachment());
        
        // Requester info from logged-in user (or fallback to username)
        letter.setRequester(requester);
        letter.setRequesterName(requester != null ? requester.getFullName() : username);
        letter.setRequesterNim(requester != null ? requester.getUsername() : username);
        
        // Borrow date/time
        letter.setBorrowDate(request.getBorrowDate());
        letter.setBorrowReturnDate(request.getBorrowReturnDate());
        
        // Status = PENDING (waiting for approval)
        letter.setStatus("PENDING");
        
        // Link to event if provided
        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event tidak ditemukan"));
            letter.setEvent(event);
        }
        
        // Auto-assign to active period
        periodRepository.findByIsActiveTrue().ifPresent(letter::setPeriod);
        
        Letter saved = letterRepository.save(letter);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.create(
                "LETTER", saved.getId(), saved.getSubject(),
                "Created letter request by: " + (requester != null ? requester.getFullName() : username)));
        
        return toResponse(saved);
    }

    @Transactional
    public LetterResponse approveLetter(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        if (!"PENDING".equals(letter.getStatus())) {
            throw new BusinessValidationException("Hanya surat dengan status PENDING yang bisa disetujui");
        }
        
        // Set issue date = today (tanggal surat = tanggal disetujui)
        LocalDate issueDate = LocalDate.now();
        letter.setIssueDate(issueDate);
        
        // Generate letter number on approval
        String letterNumber = letterNumberGenerator.generate(
                letter.getLetterType(),
                letter.getCategory(),
                issueDate);
        letter.setLetterNumber(letterNumber);
        
        // Set approved
        letter.setStatus("APPROVED");
        String approver = SecurityContextHolder.getContext().getAuthentication().getName();
        letter.setApprovedBy(approver);
        
        Letter saved = letterRepository.save(letter);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "LETTER", saved.getId(), saved.getSubject(),
                "Approved letter: " + letterNumber + " by " + approver));
        
        return toResponse(saved);
    }

    @Transactional
    public LetterResponse rejectLetter(String id, String reason) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        if (!"PENDING".equals(letter.getStatus())) {
            throw new BusinessValidationException("Hanya surat dengan status PENDING yang bisa ditolak");
        }
        
        letter.setStatus("REJECTED");
        letter.setRejectionReason(reason);
        String approver = SecurityContextHolder.getContext().getAuthentication().getName();
        letter.setApprovedBy(approver);
        
        Letter saved = letterRepository.save(letter);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "LETTER", saved.getId(), saved.getSubject(),
                "Rejected letter by " + approver + ": " + reason));
        
        return toResponse(saved);
    }

    public List<LetterResponse> getAllLetters() {
        return letterRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LetterResponse> getLettersByPeriod(String periodId) {
        return letterRepository.findByPeriodId(periodId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LetterResponse getLetterById(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        return toResponse(letter);
    }

    public LetterResponse getLetterByNumber(String letterNumber) {
        Letter letter = letterRepository.findByLetterNumber(letterNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        return toResponse(letter);
    }

    @Transactional
    public void deleteLetter(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        String subject = letter.getSubject();
        String number = letter.getLetterNumber();
        
        letterRepository.deleteById(id);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.delete(
                "LETTER", id, subject,
                "Deleted letter: " + (number != null ? number : "pending")));
    }

    // ==================== SURAT MASUK ====================
    
    @Transactional
    public IncomingLetterResponse createIncomingLetter(CreateIncomingLetterRequest request) {
        IncomingLetter letter = new IncomingLetter();
        letter.setReferenceNumber(request.getReferenceNumber());
        letter.setSender(request.getSender());
        letter.setSubject(request.getSubject());
        letter.setReceivedDate(request.getReceivedDate() != null 
                ? request.getReceivedDate() 
                : LocalDate.now());
        letter.setNotes(request.getNotes());
        
        IncomingLetter saved = incomingLetterRepository.save(letter);
        return toIncomingResponse(saved);
    }

    public List<IncomingLetterResponse> getAllIncomingLetters() {
        return incomingLetterRepository.findAll().stream()
                .map(this::toIncomingResponse)
                .collect(Collectors.toList());
    }

    public IncomingLetterResponse getIncomingLetterById(String id) {
        IncomingLetter letter = incomingLetterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat masuk tidak ditemukan"));
        return toIncomingResponse(letter);
    }

    @Transactional
    public void deleteIncomingLetter(String id) {
        if (!incomingLetterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Surat masuk tidak ditemukan");
        }
        incomingLetterRepository.deleteById(id);
    }

    // ==================== HELPERS ====================
    
    private LetterResponse toResponse(Letter letter) {
        try {
            LetterResponse.EventSummary eventSummary = null;
            if (letter.getEvent() != null) {
                eventSummary = LetterResponse.EventSummary.builder()
                        .id(letter.getEvent().getId())
                        .eventCode(letter.getEvent().getEventCode())
                        .name(letter.getEvent().getName())
                        .build();
            }
            
            return LetterResponse.builder()
                    .id(letter.getId())
                    .letterNumber(letter.getLetterNumber())
                    .letterType(letter.getLetterType())
                    .category(letter.getCategory())
                    .subject(letter.getSubject())
                    .recipient(letter.getRecipient())
                    .content(letter.getContent())
                    .attachment(letter.getAttachment())
                    .requesterName(letter.getRequesterName())
                    .requesterNim(letter.getRequesterNim())
                    .borrowDate(letter.getBorrowDate())
                    .borrowReturnDate(letter.getBorrowReturnDate())
                    .issueDate(letter.getIssueDate())
                    .status(letter.getStatus())
                    .approvedBy(letter.getApprovedBy())
                    .rejectionReason(letter.getRejectionReason())
                    .event(eventSummary)
                    .createdAt(letter.getCreatedAt())
                    .updatedAt(letter.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR converting Letter ID: " + letter.getId() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private IncomingLetterResponse toIncomingResponse(IncomingLetter letter) {
        return IncomingLetterResponse.builder()
                .id(letter.getId())
                .referenceNumber(letter.getReferenceNumber())
                .sender(letter.getSender())
                .subject(letter.getSubject())
                .receivedDate(letter.getReceivedDate())
                .notes(letter.getNotes())
                .attachmentPath(letter.getAttachmentPath())
                .createdAt(letter.getCreatedAt())
                .updatedAt(letter.getUpdatedAt())
                .build();
    }
}
