package com.mbclab.lablink.features.period;

import com.mbclab.lablink.features.period.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class PeriodController {

    private final PeriodService periodService;

    // ========== PERIOD CRUD ==========
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PeriodResponse> createPeriod(@RequestBody CreatePeriodRequest request) {
        return ResponseEntity.ok(periodService.createPeriod(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PeriodResponse>> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PeriodResponse> getActivePeriod() {
        return ResponseEntity.ok(periodService.getActivePeriod());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PeriodResponse> getPeriodById(@PathVariable String id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    // ========== ACTIVATE & CLOSE ==========
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PeriodResponse> activatePeriod(@PathVariable String id) {
        return ResponseEntity.ok(periodService.activatePeriod(id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PeriodResponse> closePeriod(
            @PathVariable String id,
            @RequestBody ClosePeriodRequest request) {
        return ResponseEntity.ok(periodService.closePeriod(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePeriod(@PathVariable String id) {
        periodService.deletePeriod(id);
        return ResponseEntity.noContent().build();
    }

    // ========== MEMBER MANAGEMENT ==========
    
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberPeriodResponse> addMemberToPeriod(
            @PathVariable String id,
            @RequestBody AddMemberToPeriodRequest request) {
        return ResponseEntity.ok(periodService.addMemberToPeriod(id, request));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberPeriodResponse>> getMembersByPeriod(@PathVariable String id) {
        return ResponseEntity.ok(periodService.getMembersByPeriod(id));
    }
}
