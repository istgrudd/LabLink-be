package com.mbclab.lablink.features.member;


import com.mbclab.lablink.shared.exception.ResourceNotFoundException;
import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.member.dto.AssignRolesRequest;
import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.MemberResponse;
import com.mbclab.lablink.features.member.dto.RoleResponse;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.period.MemberPeriod;
import com.mbclab.lablink.features.period.MemberPeriodRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final AcademicPeriodRepository periodRepository;
    private final MemberPeriodRepository memberPeriodRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CREATE ==========
    
    @Transactional
    public MemberResponse createResearchAssistant(CreateMemberRequest request) {
        if (memberRepository.existsByUsername(request.getNim())) {
            throw new com.mbclab.lablink.shared.exception.BusinessValidationException("Member dengan NIM " + request.getNim() + " sudah ada!");
        }

        ResearchAssistant newMember = new ResearchAssistant();
        newMember.setUsername(request.getNim());
        newMember.setFullName(request.getFullName());
        newMember.setRole("ASSISTANT"); // Default role in AppUser (for backward compatibility)
        
        String encryptedPassword = passwordEncoder.encode(request.getNim());
        newMember.setPassword(encryptedPassword);
        newMember.setPasswordChanged(false);
        
        newMember.setExpertDivision(request.getExpertDivision());
        newMember.setDepartment(request.getDepartment());
        
        ResearchAssistant saved = memberRepository.save(newMember);
        
        // Assign default ASSISTANT role
        MemberRole defaultRole = new MemberRole(saved, Role.ASSISTANT, "SYSTEM");
        memberRoleRepository.save(defaultRole);
        
        // Auto-associate dengan active period jika ada
        Optional<AcademicPeriod> activePeriod = periodRepository.findByIsActiveTrue();
        if (activePeriod.isPresent()) {
            MemberPeriod mp = new MemberPeriod(saved, activePeriod.get(), request.getPosition());
            memberPeriodRepository.save(mp);
        }
        
        eventPublisher.publishEvent(AuditEvent.create(
                "MEMBER", saved.getId(), saved.getFullName(),
                "Created member: " + saved.getUsername()));
        
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public Page<MemberResponse> getAllMembers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return memberRepository.findAll(pageable).map(this::toResponse);
    }
    
    public List<MemberResponse> getAllMembersUnpaginated() {
        return memberRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse getMemberByNim(String nim) {
        ResearchAssistant member = memberRepository.findByUsername(nim)
                .orElseThrow(() -> new ResourceNotFoundException("Member dengan NIM " + nim + " tidak ditemukan"));
        return toResponse(member);
    }

    public MemberResponse getMemberById(String id) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan"));
        return toResponse(member);
    }

    // ========== UPDATE ==========
    
    @Transactional
    public MemberResponse updateMember(String id, UpdateMemberRequest request) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member dengan ID " + id + " tidak ditemukan"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            member.setFullName(request.getFullName());
        }
        if (request.getExpertDivision() != null && !request.getExpertDivision().isBlank()) {
            member.setExpertDivision(request.getExpertDivision());
        }
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            member.setDepartment(request.getDepartment());
        }
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) member.setPhoneNumber(request.getPhoneNumber());
        if (request.getSocialMediaLink() != null) member.setSocialMediaLink(request.getSocialMediaLink());

        ResearchAssistant saved = memberRepository.save(member);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "MEMBER", saved.getId(), saved.getFullName(),
                "Updated member: " + saved.getUsername()));
        
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional 
    public void deleteMember(String id) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member ID " + id + " tidak ditemukan"));
        String name = member.getFullName();
        String nim = member.getUsername();
        
        try {
            // Delete roles first
            memberRoleRepository.deleteByMemberId(id);
            memberRepository.deleteById(id);
            
            eventPublisher.publishEvent(AuditEvent.delete(
                    "MEMBER", id, name,
                    "Deleted member: " + nim));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException(
                "Tidak dapat menghapus member " + name + " karena masih terdaftar sebagai ketua proyek atau terlibat dalam data lain."
            );
        }
    }

    // ========== ROLE MANAGEMENT ==========
    
    public List<RoleResponse> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(r -> RoleResponse.builder()
                        .role(r.name())
                        .displayName(r.getDisplayName())
                        .description(r.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public List<MemberResponse.RoleInfo> getMemberRoles(String memberId) {
        return memberRoleRepository.findByMemberId(memberId).stream()
                .map(mr -> MemberResponse.RoleInfo.builder()
                        .role(mr.getRole().name())
                        .displayName(mr.getRole().getDisplayName())
                        .description(mr.getRole().getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse assignRoles(String memberId, AssignRolesRequest request, String assignedBy) {
        ResearchAssistant member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        // Clear existing roles
        memberRoleRepository.deleteByMemberId(memberId);
        
        // Assign new roles
        for (String roleName : request.getRoles()) {
            try {
                Role role = Role.valueOf(roleName.toUpperCase());
                MemberRole mr = new MemberRole(member, role, assignedBy);
                memberRoleRepository.save(mr);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Role tidak valid: " + roleName);
            }
        }
        
        eventPublisher.publishEvent(AuditEvent.update(
                "MEMBER", member.getId(), member.getFullName(),
                "Updated roles to: " + String.join(", ", request.getRoles())));
        
        return toResponse(member);
    }

    public boolean hasRole(String memberId, Role role) {
        return memberRoleRepository.existsByMemberIdAndRole(memberId, role);
    }

    public boolean hasAnyRole(String memberId, Role... roles) {
        for (Role role : roles) {
            if (memberRoleRepository.existsByMemberIdAndRole(memberId, role)) {
                return true;
            }
        }
        return false;
    }

    // ========== HELPER ==========
    
    private MemberResponse toResponse(ResearchAssistant member) {
        List<MemberResponse.RoleInfo> roles = member.getMemberRoles().stream()
                .map(mr -> MemberResponse.RoleInfo.builder()
                        .role(mr.getRole().name())
                        .displayName(mr.getRole().getDisplayName())
                        .description(mr.getRole().getDescription())
                        .build())
                .collect(Collectors.toList());
        
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .fullName(member.getFullName())
                .roles(roles)
                .expertDivision(member.getExpertDivision())
                .department(member.getDepartment())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .socialMediaLink(member.getSocialMediaLink())
                .isActive(member.isActive())
                .isPasswordChanged(member.isPasswordChanged())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
