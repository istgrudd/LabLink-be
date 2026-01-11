package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. Create Member (Hanya Admin)
    @PostMapping
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResearchAssistant> createMember(@RequestBody CreateMemberRequest request) {
        ResearchAssistant created = memberService.createResearchAssistant(request);
        return ResponseEntity.ok(created);
    }

    // 2. Get All Members (Bisa Admin & Asisten lain)
    @GetMapping
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResearchAssistant>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // 3. Update Member
    // URL: PUT /api/members/{id}
    // Contoh: /api/members/a1b2-c3d4-e5f6 (Pakai UUID)
    @PutMapping("/{id}")
    // @PreAuthorize("isAuthenticated()") <-- Nanti dinyalakan
    public ResponseEntity<ResearchAssistant> updateMember(
            @PathVariable String id, 
            @RequestBody UpdateMemberRequest request) {
            
        ResearchAssistant updated = memberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')") <-- Nanti dinyalakan
    public ResponseEntity<Void> deleteMember(@PathVariable String id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build(); // Return 204 No Content (Standard Delete)
    }
}
