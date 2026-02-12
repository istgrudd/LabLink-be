package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.auth.AppUser;
import com.mbclab.lablink.features.auth.AuthService;
import com.mbclab.lablink.features.member.dto.AssignRolesRequest;
import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.MemberResponse;
import com.mbclab.lablink.features.member.dto.RoleResponse;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    // ========== CREATE ==========
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        return ResponseEntity.ok(memberService.createResearchAssistant(request));
    }

    // ========== READ ==========
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(memberService.getAllMembers(page, size));
    }
    
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberResponse>> getAllMembersUnpaginated() {
        return ResponseEntity.ok(memberService.getAllMembersUnpaginated());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable String id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping("/nim/{nim}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberResponse> getMemberByNim(@PathVariable String nim) {
        return ResponseEntity.ok(memberService.getMemberByNim(nim));
    }

    // ========== UPDATE ==========
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable String id, 
            @Valid @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    // ========== DELETE ==========
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable String id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    // ========== ROLE MANAGEMENT ==========
    
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(memberService.getAllRoles());
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MemberResponse.RoleInfo>> getMemberRoles(@PathVariable String id) {
        return ResponseEntity.ok(memberService.getMemberRoles(id));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> assignRoles(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AssignRolesRequest request) {
        String token = authHeader.substring(7);
        AppUser admin = authService.validateToken(token);
        return ResponseEntity.ok(memberService.assignRoles(id, request, admin.getUsername()));
    }
}

