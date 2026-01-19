package com.mbclab.lablink.features.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbclab.lablink.features.auth.AppUser;
import com.mbclab.lablink.features.auth.AuthService;
import com.mbclab.lablink.features.finance.dto.*;
import com.mbclab.lablink.shared.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // ==================== CATEGORY ====================

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(financeService.createCategory(request));
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(financeService.getAllCategories());
    }

    @GetMapping("/categories/by-type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(@PathVariable String type) {
        return ResponseEntity.ok(financeService.getCategoriesByType(type));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String id, 
            @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(financeService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        financeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DUES PAYMENT ====================

    @PostMapping(value = "/dues", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DuesPaymentResponse> submitDuesPayment(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("data") String duesDataJson,
            @RequestParam("file") MultipartFile file) {
        try {
            AppUser user = getUserFromToken(authHeader);
            DuesPaymentRequest request = objectMapper.readValue(duesDataJson, DuesPaymentRequest.class);
            String proofPath = fileStorageService.storeFile(file);
            
            return ResponseEntity.ok(financeService.submitDuesPayment(user.getId(), request, proofPath));
        } catch (Exception e) {
            throw new RuntimeException("Gagal submit pembayaran: " + e.getMessage());
        }
    }

    @GetMapping("/dues/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DuesPaymentResponse>> getMyDuesHistory(
            @RequestHeader("Authorization") String authHeader) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.getMyDuesHistory(user.getId()));
    }

    @GetMapping("/dues")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<List<DuesPaymentResponse>> getAllDues() {
        return ResponseEntity.ok(financeService.getAllDues());
    }

    @GetMapping("/dues/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<List<DuesPaymentResponse>> getPendingVerification() {
        return ResponseEntity.ok(financeService.getPendingVerification());
    }

    @PutMapping("/dues/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<DuesPaymentResponse> verifyDuesPayment(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.verifyDuesPayment(id, admin.getUsername()));
    }

    // ==================== TRANSACTIONS ====================

    @PostMapping(value = "/transactions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> createTransactionWithReceipt(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("data") String transactionDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            AppUser admin = getUserFromToken(authHeader);
            TransactionRequest request = objectMapper.readValue(transactionDataJson, TransactionRequest.class);
            String receiptPath = file != null ? fileStorageService.storeFile(file) : null;
            
            return ResponseEntity.ok(financeService.createTransaction(request, receiptPath, admin.getUsername()));
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat transaksi: " + e.getMessage());
        }
    }

    @PostMapping("/transactions/simple")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> createTransactionSimple(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransactionRequest request) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.createTransaction(request, null, admin.getUsername()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(financeService.getAllTransactions(page, size));
    }

    @GetMapping("/transactions/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary() {
        return ResponseEntity.ok(financeService.getTransactionSummary());
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable String id,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(financeService.updateTransaction(id, request));
    }

    @DeleteMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        financeService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PROCUREMENT ====================

    @PostMapping("/procurement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProcurementResponse> createProcurementRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProcurementRequestDto request) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.createProcurementRequest(user.getId(), request));
    }

    @GetMapping("/procurement/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProcurementResponse>> getMyProcurementRequests(
            @RequestHeader("Authorization") String authHeader) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.getMyProcurementRequests(user.getId()));
    }

    @GetMapping("/procurement")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Page<ProcurementResponse>> getAllProcurementRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(financeService.getAllProcurementRequests(page, size));
    }

    @GetMapping("/procurement/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<List<ProcurementResponse>> getPendingProcurements() {
        return ResponseEntity.ok(financeService.getPendingProcurements());
    }

    @PutMapping("/procurement/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> approveProcurement(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.approveProcurement(id, admin.getUsername()));
    }

    @PutMapping("/procurement/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> rejectProcurement(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RejectProcurementRequest request) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(financeService.rejectProcurement(id, admin.getUsername(), request.getRejectionReason()));
    }

    @PutMapping("/procurement/{id}/mark-purchased")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> markPurchased(
            @PathVariable String id,
            @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(financeService.markPurchased(id, transactionId));
    }

    // ==================== HELPER ====================

    private AppUser getUserFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return authService.validateToken(token);
    }
}
