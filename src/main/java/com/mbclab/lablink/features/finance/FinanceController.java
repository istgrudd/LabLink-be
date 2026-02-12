package com.mbclab.lablink.features.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbclab.lablink.features.auth.AppUser;
import com.mbclab.lablink.features.auth.AuthService;
import com.mbclab.lablink.features.finance.dto.*;
import jakarta.validation.Valid;
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

    private final FinanceCategoryService categoryService;
    private final DuesPaymentService duesPaymentService;
    private final FinanceTransactionService transactionService;
    private final ProcurementService procurementService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    // ==================== CATEGORY ====================

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/by-type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(@PathVariable String type) {
        return ResponseEntity.ok(categoryService.getCategoriesByType(type));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String id, 
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DUES PAYMENT ====================

    @PostMapping(value = "/dues", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DuesPaymentResponse> submitDuesPayment(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("data") String duesDataJson,
            @RequestParam("file") MultipartFile file) throws Exception {
        AppUser user = getUserFromToken(authHeader);
        DuesPaymentRequest request = objectMapper.readValue(duesDataJson, DuesPaymentRequest.class);
        return ResponseEntity.ok(duesPaymentService.submitDuesPayment(user.getId(), request, file));
    }

    @GetMapping("/dues/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DuesPaymentResponse>> getMyDuesHistory(
            @RequestHeader("Authorization") String authHeader) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(duesPaymentService.getMyDuesHistory(user.getId()));
    }

    @GetMapping("/dues")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Page<DuesPaymentResponse>> getAllDues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(duesPaymentService.getAllDues(page, size));
    }

    @GetMapping("/dues/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<List<DuesPaymentResponse>> getPendingVerification() {
        return ResponseEntity.ok(duesPaymentService.getPendingVerification());
    }

    @PutMapping("/dues/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<DuesPaymentResponse> verifyDuesPayment(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(duesPaymentService.verifyDuesPayment(id, admin.getUsername()));
    }

    @PostMapping("/dues/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<DuesPaymentResponse> rejectDuesPayment(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(duesPaymentService.rejectDuesPayment(id, admin.getUsername()));
    }

    // ==================== TRANSACTIONS ====================

    @PostMapping(value = "/transactions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> createTransactionWithReceipt(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("data") String transactionDataJson,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        AppUser admin = getUserFromToken(authHeader);
        TransactionRequest request = objectMapper.readValue(transactionDataJson, TransactionRequest.class);
        return ResponseEntity.ok(transactionService.createTransaction(request, file, admin.getUsername()));
    }

    @PostMapping("/transactions/simple")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> createTransactionSimple(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TransactionRequest request) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(transactionService.createTransaction(request, admin.getUsername()));
    }

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size));
    }

    @GetMapping("/transactions/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary() {
        return ResponseEntity.ok(transactionService.getTransactionSummary());
    }

    @PutMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PROCUREMENT ====================

    @PostMapping("/procurement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProcurementResponse> createProcurementRequest(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProcurementRequestDto request) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(procurementService.createProcurementRequest(user.getId(), request));
    }

    @GetMapping("/procurement/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProcurementResponse>> getMyProcurementRequests(
            @RequestHeader("Authorization") String authHeader) {
        AppUser user = getUserFromToken(authHeader);
        return ResponseEntity.ok(procurementService.getMyProcurementRequests(user.getId()));
    }

    @GetMapping("/procurement")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<Page<ProcurementResponse>> getAllProcurementRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(procurementService.getAllProcurementRequests(page, size));
    }

    @GetMapping("/procurement/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<List<ProcurementResponse>> getPendingProcurements() {
        return ResponseEntity.ok(procurementService.getPendingProcurements());
    }

    @PutMapping("/procurement/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> approveProcurement(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(procurementService.approveProcurement(id, admin.getUsername()));
    }

    @PutMapping("/procurement/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> rejectProcurement(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RejectProcurementRequest request) {
        AppUser admin = getUserFromToken(authHeader);
        return ResponseEntity.ok(procurementService.rejectProcurement(id, admin.getUsername(), request.getRejectionReason()));
    }

    @PutMapping("/procurement/{id}/mark-purchased")
    @PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
    public ResponseEntity<ProcurementResponse> markPurchased(
            @PathVariable String id,
            @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(procurementService.markPurchased(id, transactionId));
    }

    // ==================== HELPER ====================

    private AppUser getUserFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return authService.validateToken(token);
    }
}
