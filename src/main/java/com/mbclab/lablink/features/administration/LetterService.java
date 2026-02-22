package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.administration.dto.*;
import com.mbclab.lablink.shared.exception.BusinessValidationException;
import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import com.mbclab.lablink.shared.exception.FileStorageException;
import com.mbclab.lablink.shared.status.LetterStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LetterService {

    private final LetterRepository letterRepository;
    private final IncomingLetterRepository incomingLetterRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final LetterNumberGenerator letterNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final LetterDocumentGenerator letterDocumentGenerator;

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
        letter.setStatus(LetterStatus.PENDING);
        
        // Link to event if provided
        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event tidak ditemukan"));
            letter.setEvent(event);
        }
        
        Letter saved = letterRepository.save(letter);
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.create(
                "LETTER", saved.getId(), saved.getSubject(),
                "Created letter request by: " + (requester != null ? requester.getFullName() : username)));
        
        return toResponse(saved);
    }

    @Transactional
    public LetterResponse reviewLetter(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        if (letter.getStatus() != LetterStatus.PENDING) {
            throw new BusinessValidationException("Hanya surat dengan status PENDING yang bisa di-review");
        }
        
        letter.setStatus(LetterStatus.REVIEWED);
        String reviewer = SecurityContextHolder.getContext().getAuthentication().getName();
        letter.setReviewedBy(reviewer);
        
        Letter saved = letterRepository.save(letter);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "LETTER", saved.getId(), saved.getSubject(),
                "Reviewed letter by " + reviewer));
        
        return toResponse(saved);
    }

    @Transactional
    public LetterResponse approveLetter(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        if (letter.getStatus() != LetterStatus.REVIEWED) {
            throw new BusinessValidationException("Hanya surat dengan status REVIEWED yang bisa disetujui. " +
                    "Surat harus di-review terlebih dahulu oleh Sekretaris.");
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
        letter.setStatus(LetterStatus.APPROVED);
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
    public LetterResponse signLetter(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        if (letter.getStatus() != LetterStatus.APPROVED) {
            throw new BusinessValidationException("Hanya surat dengan status APPROVED yang bisa ditandatangani. " +
                    "Surat harus di-approve terlebih dahulu.");
        }
        
        letter.setStatus(LetterStatus.SIGNED);
        String signer = SecurityContextHolder.getContext().getAuthentication().getName();
        letter.setSignedBy(signer);
        
        Letter saved = letterRepository.save(letter);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "LETTER", saved.getId(), saved.getSubject(),
                "Signed letter: " + saved.getLetterNumber() + " by " + signer));
        
        return toResponse(saved);
    }

    @Transactional
    public LetterResponse rejectLetter(String id, String reason) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        // Allow reject from PENDING or REVIEWED
        if (letter.getStatus() != LetterStatus.PENDING && letter.getStatus() != LetterStatus.REVIEWED) {
            throw new BusinessValidationException(
                    "Hanya surat dengan status PENDING atau REVIEWED yang bisa ditolak");
        }
        
        letter.setStatus(LetterStatus.REJECTED);
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

    public Page<LetterResponse> getAllLetters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return letterRepository.findAll(pageable)
                .map(this::toResponse);
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

    // ==================== DOCUMENT GENERATION ====================

    /**
     * Generate document from letter data.
     * All business logic (status check, template data mapping) is in the service layer.
     */
    public byte[] generateDocument(String id, String templateName) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        
        // Status validation — only approved or signed letters can be downloaded
        if (letter.getStatus() != LetterStatus.APPROVED && letter.getStatus() != LetterStatus.SIGNED) {
            throw new BusinessValidationException(
                    "Hanya surat yang sudah disetujui/ditandatangani yang bisa didownload");
        }
        
        // Build template data from entity (SoC — not in controller)
        Map<String, String> data = new HashMap<>();
        data.put("perihal", letter.getSubject());
        data.put("tujuan", letter.getRecipient());
        data.put("isi_surat", letter.getContent() != null ? letter.getContent() : "");
        data.put("lampiran", letter.getAttachment() != null ? letter.getAttachment() : "-");
        
        // Requester info
        data.put("nama_pemohon", letter.getRequesterName() != null ? letter.getRequesterName() : "");
        data.put("nim_pemohon", letter.getRequesterNim() != null ? letter.getRequesterNim() : "");
        
        // Event / activity name
        data.put("nama_kegiatan", letter.getEvent() != null ? letter.getEvent().getName() : "");
        
        // Borrow dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                "d MMMM yyyy", new java.util.Locale("id", "ID"));
        data.put("waktu_mulai", letter.getBorrowDate() != null 
                ? letter.getBorrowDate().format(dateFormatter) : "");
        data.put("waktu_selesai", letter.getBorrowReturnDate() != null 
                ? letter.getBorrowReturnDate().format(dateFormatter) : "");
        
        try {
            return letterDocumentGenerator.generateDocument(
                    templateName, letter.getLetterType(), letter.getCategory(), data);
        } catch (IOException e) {
            throw new FileStorageException("Gagal generate dokumen surat: " + e.getMessage(), e);
        }
    }

    /**
     * Get the letter number formatted as filename.
     */
    public String getLetterFilename(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Surat tidak ditemukan"));
        return "Surat_" + (letter.getLetterNumber() != null 
                ? letter.getLetterNumber().replace("/", "-") 
                : letter.getId()) + ".docx";
    }

    // ==================== SURAT MASUK ====================
    
    @Transactional
    public IncomingLetterResponse createIncomingLetter(CreateIncomingLetterRequest request) {
        IncomingLetter letter = new IncomingLetter();
        
        // Handle optional reference number (generate if missing)
        String refNum = request.getReferenceNumber();
        if (refNum == null || refNum.isBlank()) {
            refNum = "INC-" + System.currentTimeMillis();
        }
        letter.setReferenceNumber(refNum);
        
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
                    .status(letter.getStatus().name())
                    .createdBy(letter.getRequesterName())
                    .reviewedBy(letter.getReviewedBy())
                    .approvedBy(letter.getApprovedBy())
                    .signedBy(letter.getSignedBy())
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
