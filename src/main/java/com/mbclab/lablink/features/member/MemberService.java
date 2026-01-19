package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.MemberResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final AcademicPeriodRepository periodRepository;
    private final MemberPeriodRepository memberPeriodRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CREATE ==========
    
    @Transactional
    public MemberResponse createResearchAssistant(CreateMemberRequest request) {
        // 1. Validasi: NIM tidak boleh kembar
        if (memberRepository.existsByUsername(request.getNim())) {
            throw new RuntimeException("Member dengan NIM " + request.getNim() + " sudah ada!");
        }

        // 2. Buat Object Baru
        ResearchAssistant newMember = new ResearchAssistant();
        
        // 3. Set Data Identitas
        newMember.setUsername(request.getNim());
        newMember.setFullName(request.getFullName());
        // Use role from request if provided, otherwise default to ASSISTANT
        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() 
                : "ASSISTANT";
        newMember.setRole(role);
        
        // 4. Set Password Default (= NIM) & Enkripsi
        String encryptedPassword = passwordEncoder.encode(request.getNim());
        newMember.setPassword(encryptedPassword);
        newMember.setPasswordChanged(false);
        
        // 5. Set Data Spesifik
        newMember.setExpertDivision(request.getExpertDivision());
        newMember.setDepartment(request.getDepartment());
        
        // 6. Simpan member
        ResearchAssistant saved = memberRepository.save(newMember);
        
        // 7. Auto-associate dengan active period jika ada
        Optional<AcademicPeriod> activePeriod = periodRepository.findByIsActiveTrue();
        if (activePeriod.isPresent()) {
            MemberPeriod mp = new MemberPeriod(saved, activePeriod.get(), request.getPosition());
            memberPeriodRepository.save(mp);
        }
        
        // Publish audit event
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
                .orElseThrow(() -> new RuntimeException("Member dengan NIM " + nim + " tidak ditemukan"));
        return toResponse(member);
    }

    public MemberResponse getMemberById(String id) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        return toResponse(member);
    }

    public List<MemberResponse> getOrphanMembers() {
        // Get all member IDs that are in at least one period
        List<String> memberIdsInPeriods = memberPeriodRepository.findAll().stream()
                .map(mp -> mp.getMember().getId())
                .distinct()
                .collect(Collectors.toList());
        
        // Get all members not in that list
        return memberRepository.findAll().stream()
                .filter(m -> !memberIdsInPeriods.contains(m.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ========== UPDATE ==========
    
    @Transactional
    public MemberResponse updateMember(String id, UpdateMemberRequest request) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member dengan ID " + id + " tidak ditemukan"));

        // Partial Update
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
        
        // Publish audit event
        eventPublisher.publishEvent(AuditEvent.update(
                "MEMBER", saved.getId(), saved.getFullName(),
                "Updated member: " + saved.getUsername()));
        
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional 
    public void deleteMember(String id) {
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member ID " + id + " tidak ditemukan"));
        String name = member.getFullName();
        String nim = member.getUsername();
        
        try {
            memberRepository.deleteById(id);
            
            // Publish audit event
            eventPublisher.publishEvent(AuditEvent.delete(
                    "MEMBER", id, name,
                    "Deleted member: " + nim));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException(
                "Tidak dapat menghapus member " + name + " karena masih terdaftar sebagai ketua proyek atau terlibat dalam data lain. " +
                "Harap hapus atau ubah ketua proyek terlebih dahulu."
            );
        }
    }

    // ========== HELPER: Convert to Response DTO ==========
    
    private MemberResponse toResponse(ResearchAssistant member) {
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .fullName(member.getFullName())
                .role(member.getRole())
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
