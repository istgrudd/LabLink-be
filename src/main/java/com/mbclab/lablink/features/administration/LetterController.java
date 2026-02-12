package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.features.administration.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administration/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    // ==================== SURAT KELUAR ====================
    
    // All authenticated users can request a letter
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LetterResponse> createLetter(@Valid @RequestBody CreateLetterRequest request) {
        return ResponseEntity.ok(letterService.createLetter(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LetterResponse>> getAllLetters(
            @RequestParam(required = false) String periodId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (periodId != null && !periodId.isBlank()) {
            return ResponseEntity.ok(letterService.getLettersByPeriod(periodId, page, size));
        }
        return ResponseEntity.ok(letterService.getAllLetters(page, size));
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

    // Sekretaris reviews letter request (PENDING → REVIEWED)
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<LetterResponse> reviewLetter(@PathVariable String id) {
        return ResponseEntity.ok(letterService.reviewLetter(id));
    }

    // Ketua/Dosen approves letter request (REVIEWED → APPROVED)
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEKRETARIS')")
    public ResponseEntity<LetterResponse> approveLetter(@PathVariable String id) {
        return ResponseEntity.ok(letterService.approveLetter(id));
    }

    // Sign the letter (APPROVED → SIGNED)
    @PatchMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('ADMIN', 'HEAD_LAB')")
    public ResponseEntity<LetterResponse> signLetter(@PathVariable String id) {
        return ResponseEntity.ok(letterService.signLetter(id));
    }

    // Admin rejects letter request (from PENDING or REVIEWED)
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

    // Download approved/signed letter — controller only handles HTTP response
    @PostMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadLetter(
            @PathVariable String id,
            @RequestParam(defaultValue = "Surat Peminjaman Videotron MBC") String templateName) {
        byte[] document = letterService.generateDocument(id, templateName);
        String filename = letterService.getLetterFilename(id);
        
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
            @Valid @RequestBody CreateIncomingLetterRequest request) {
        return ResponseEntity.ok(letterService.createIncomingLetter(request));
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
