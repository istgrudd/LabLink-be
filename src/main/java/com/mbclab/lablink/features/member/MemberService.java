package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.MemberResponse;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
        newMember.setRole("ASSISTANT");
        
        // 4. Set Password Default (= NIM) & Enkripsi
        String encryptedPassword = passwordEncoder.encode(request.getNim());
        newMember.setPassword(encryptedPassword);
        newMember.setPasswordChanged(false);
        
        // 5. Set Data Spesifik
        newMember.setExpertDivision(request.getExpertDivision());
        newMember.setDepartment(request.getDepartment());
        
        // 6. Simpan dan return response
        ResearchAssistant saved = memberRepository.save(newMember);
        return toResponse(saved);
    }

    // ========== READ ==========
    
    public List<MemberResponse> getAllMembers() {
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
        return toResponse(saved);
    }

    // ========== DELETE ==========
    
    @Transactional 
    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member ID " + id + " tidak ditemukan");
        }
        memberRepository.deleteById(id);
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
