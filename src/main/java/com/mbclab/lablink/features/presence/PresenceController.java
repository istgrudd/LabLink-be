package com.mbclab.lablink.features.presence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbclab.lablink.features.auth.AppUser;
import com.mbclab.lablink.features.auth.AuthService;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.presence.dto.CreatePresenceRequest;
import com.mbclab.lablink.features.presence.dto.PresenceResponse;
import com.mbclab.lablink.shared.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceRepository presenceRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PresenceResponse> createPresence(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("data") String presenceDataJson,
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. Get User
            AppUser user = getUserFromToken(authHeader);
            ResearchAssistant member = memberRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            // 2. Parse JSON Data
            CreatePresenceRequest request = objectMapper.readValue(presenceDataJson, CreatePresenceRequest.class);

            // 3. Store File
            String fileName = fileStorageService.storeFile(file);

            // 4. Create Entity
            Presence presence = new Presence();
            presence.setMember(member);
            presence.setType(Presence.PresenceType.valueOf(request.getType()));
            presence.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
            presence.setTitle(request.getTitle() != null ? request.getTitle() : "Presence");
            presence.setImagePath(fileName);
            presence.setNotes(request.getNotes());

            Presence saved = presenceRepository.save(presence);

            return ResponseEntity.ok(toResponse(saved));

        } catch (Exception e) {
            throw new RuntimeException("Failed to create presence: " + e.getMessage());
        }
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PresenceResponse>> getMyHistory(@RequestHeader("Authorization") String authHeader) {
        AppUser user = getUserFromToken(authHeader);
        
        List<Presence> history = presenceRepository.findByMemberIdOrderByDateDesc(user.getId());
        
        return ResponseEntity.ok(history.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PresenceResponse>> getAllPresence(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        
        // Default to current month if no dates provided
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        List<Presence> results;
        if (type != null && !type.isBlank()) {
            results = presenceRepository.findByTypeAndDateBetween(
                    Presence.PresenceType.valueOf(type), startDate, endDate);
        } else {
            results = presenceRepository.findByDateBetween(startDate, endDate);
        }
        
        return ResponseEntity.ok(results.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    private AppUser getUserFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return authService.validateToken(token);
    }

    private PresenceResponse toResponse(Presence p) {
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(p.getImagePath())
                .toUriString();

        return PresenceResponse.builder()
                .id(p.getId())
                .memberName(p.getMember().getFullName())
                .type(p.getType().name())
                .date(p.getDate())
                .title(p.getTitle())
                .imageUrl(fileDownloadUri)
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
