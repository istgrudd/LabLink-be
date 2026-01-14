package com.mbclab.lablink.features.letter;

import com.mbclab.lablink.features.letter.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    // ==================== SURAT KELUAR ====================
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LetterResponse> createLetter(@RequestBody CreateLetterRequest request) {
        LetterResponse created = letterService.createLetter(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LetterResponse>> getAllLetters() {
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
        // Replace dashes with slashes (URL encoding workaround)
        String normalizedNumber = letterNumber.replace("-", "/");
        return ResponseEntity.ok(letterService.getLetterByNumber(normalizedNumber));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LetterResponse> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return ResponseEntity.ok(letterService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLetter(@PathVariable String id) {
        letterService.deleteLetter(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== SURAT MASUK ====================
    
    @PostMapping("/incoming")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIncomingLetter(@PathVariable String id) {
        letterService.deleteIncomingLetter(id);
        return ResponseEntity.noContent().build();
    }
}
