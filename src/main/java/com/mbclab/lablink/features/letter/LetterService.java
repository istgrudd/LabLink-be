package com.mbclab.lablink.features.letter;

import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.letter.dto.*;
import lombok.RequiredArgsConstructor;
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
    private final LetterNumberGenerator letterNumberGenerator;

    // ==================== SURAT KELUAR ====================
    
    @Transactional
    public LetterResponse createLetter(CreateLetterRequest request) {
        LocalDate issueDate = request.getIssueDate() != null 
                ? request.getIssueDate() 
                : LocalDate.now();
        
        // Generate letter number
        String letterNumber = letterNumberGenerator.generate(
                request.getLetterType(),
                request.getCategory(),
                issueDate);
        
        Letter letter = new Letter();
        letter.setLetterNumber(letterNumber);
        letter.setLetterType(request.getLetterType().toUpperCase());
        letter.setCategory(request.getCategory().toUpperCase());
        letter.setSubject(request.getSubject());
        letter.setRecipient(request.getRecipient());
        letter.setContent(request.getContent());
        letter.setAttachment(request.getAttachment());
        letter.setIssueDate(issueDate);
        letter.setStatus("DRAFT");
        letter.setCreatedBy(request.getCreatedBy());
        
        // Link to event if provided
        if (request.getEventId() != null && !request.getEventId().isBlank()) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));
            letter.setEvent(event);
        }
        
        Letter saved = letterRepository.save(letter);
        return toResponse(saved);
    }

    public List<LetterResponse> getAllLetters() {
        return letterRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LetterResponse getLetterById(String id) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Surat tidak ditemukan"));
        return toResponse(letter);
    }

    public LetterResponse getLetterByNumber(String letterNumber) {
        Letter letter = letterRepository.findByLetterNumber(letterNumber)
                .orElseThrow(() -> new RuntimeException("Surat tidak ditemukan"));
        return toResponse(letter);
    }

    @Transactional
    public LetterResponse updateStatus(String id, String status) {
        Letter letter = letterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Surat tidak ditemukan"));
        letter.setStatus(status.toUpperCase());
        Letter saved = letterRepository.save(letter);
        return toResponse(saved);
    }

    @Transactional
    public void deleteLetter(String id) {
        if (!letterRepository.existsById(id)) {
            throw new RuntimeException("Surat tidak ditemukan");
        }
        letterRepository.deleteById(id);
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
                .orElseThrow(() -> new RuntimeException("Surat masuk tidak ditemukan"));
        return toIncomingResponse(letter);
    }

    @Transactional
    public void deleteIncomingLetter(String id) {
        if (!incomingLetterRepository.existsById(id)) {
            throw new RuntimeException("Surat masuk tidak ditemukan");
        }
        incomingLetterRepository.deleteById(id);
    }

    // ==================== HELPERS ====================
    
    private LetterResponse toResponse(Letter letter) {
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
                .issueDate(letter.getIssueDate())
                .status(letter.getStatus())
                .createdBy(letter.getCreatedBy())
                .event(eventSummary)
                .createdAt(letter.getCreatedAt())
                .updatedAt(letter.getUpdatedAt())
                .build();
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
