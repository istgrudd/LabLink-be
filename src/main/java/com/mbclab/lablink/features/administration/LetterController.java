package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.administration.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/administration/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;
    private final LetterDocumentGenerator letterDocumentGenerator;

    // ==================== SURAT KELUAR ====================
    
    // All authenticated users can request a letter
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LetterResponse> createLetter(@RequestBody CreateLetterRequest request) {
        LetterResponse created = letterService.createLetter(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LetterResponse>> getAllLetters(
            @RequestParam(required = false) String periodId) {
        if (periodId != null && !periodId.isBlank()) {
            return ResponseEntity.ok(letterService.getLettersByPeriod(periodId));
        }
        return ResponseEntity.ok(letterService.getAllLetters());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LetterResponse> getLetterById(@PathVariable String id) {
        return ResponseEntity.ok(letterService.getLetterById(id));
    }

    @GetMapping("/number/{letterNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LetterResponse> getLetterByNumber(@PathVariable String letterNumber) {
        String normalizedNumber = letterNumber.replace("-", "/");
        return ResponseEntity.ok(letterService.getLetterByNumber(normalizedNumber));
    }

    // Admin approves letter request
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<LetterResponse> approveLetter(@PathVariable String id) {
        return ResponseEntity.ok(letterService.approveLetter(id));
    }

    // Admin rejects letter request
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<LetterResponse> rejectLetter(
            @PathVariable String id,
            @RequestParam(defaultValue = "Tidak memenuhi syarat") String reason) {
        return ResponseEntity.ok(letterService.rejectLetter(id, reason));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<Void> deleteLetter(@PathVariable String id) {
        letterService.deleteLetter(id);
        return ResponseEntity.noContent().build();
    }

    // Download approved letter (must be APPROVED status)
    @PostMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadLetter(
            @PathVariable String id,
            @RequestParam(defaultValue = "Surat Peminjaman Videotron MBC") String templateName) throws IOException {
        
        LetterResponse letter = letterService.getLetterById(id);
        
        // Only approved letters can be downloaded
        if (!"APPROVED".equals(letter.getStatus())) {
            throw new RuntimeException("Hanya surat yang sudah disetujui yang bisa didownload");
        }
        
        // Prepare data for template from letter entity (no manual input needed)
        Map<String, String> data = new HashMap<>();
        data.put("perihal", letter.getSubject());
        data.put("tujuan", letter.getRecipient());
        data.put("isi_surat", letter.getContent() != null ? letter.getContent() : "");
        data.put("lampiran", letter.getAttachment() != null ? letter.getAttachment() : "-");
        
        // Requester info (auto-filled from user when created)
        data.put("nama_pemohon", letter.getRequesterName() != null ? letter.getRequesterName() : "");
        data.put("nim_pemohon", letter.getRequesterNim() != null ? letter.getRequesterNim() : "");
        
        // Event / activity name
        if (letter.getEvent() != null) {
            data.put("nama_kegiatan", letter.getEvent().getName());
        } else {
            data.put("nama_kegiatan", "");
        }
        
        // Borrow date/return date (now using LocalDate, not LocalTime)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new java.util.Locale("id", "ID"));
        if (letter.getBorrowDate() != null) {
            data.put("waktu_mulai", letter.getBorrowDate().format(dateFormatter));
        } else {
            data.put("waktu_mulai", "");
        }
        if (letter.getBorrowReturnDate() != null) {
            data.put("waktu_selesai", letter.getBorrowReturnDate().format(dateFormatter));
        } else {
            data.put("waktu_selesai", "");
        }
        
        // Generate document
        byte[] document = letterDocumentGenerator.generateDocument(
                templateName, 
                letter.getLetterType(), 
                letter.getCategory(), 
                data);
        
        // Filename
        String filename = "Surat_" + letter.getLetterNumber().replace("/", "-") + ".docx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }

    // ==================== SURAT MASUK ====================
    
    @PostMapping("/incoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<IncomingLetterResponse> createIncomingLetter(
            @RequestBody CreateIncomingLetterRequest request) {
        IncomingLetterResponse created = letterService.createIncomingLetter(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/incoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IncomingLetterResponse>> getAllIncomingLetters() {
        return ResponseEntity.ok(letterService.getAllIncomingLetters());
    }

    @GetMapping("/incoming/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IncomingLetterResponse> getIncomingLetterById(@PathVariable String id) {
        return ResponseEntity.ok(letterService.getIncomingLetterById(id));
    }

    @DeleteMapping("/incoming/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<Void> deleteIncomingLetter(@PathVariable String id) {
        letterService.deleteIncomingLetter(id);
        return ResponseEntity.noContent().build();
    }
}
