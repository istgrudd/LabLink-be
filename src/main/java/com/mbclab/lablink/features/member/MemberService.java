package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // Inject Bean Security

    @Transactional
    public ResearchAssistant createResearchAssistant(CreateMemberRequest request) {
        // 1. Validasi: NIM tidak boleh kembar
        if (memberRepository.existsByUsername(request.getNim())) {
            throw new RuntimeException("Member dengan NIM " + request.getNim() + " sudah ada!");
        }

        // 2. Buat Object Baru
        ResearchAssistant newMember = new ResearchAssistant();
        
        // 3. Set Data Identitas (Parent AppUser)
        newMember.setUsername(request.getNim());
        newMember.setFullName(request.getFullName());
        newMember.setRole("ASSISTANT"); // Default Role
        
        // 4. Set Password Default (= NIM) & Enkripsi
        String encryptedPassword = passwordEncoder.encode(request.getNim());
        newMember.setPassword(encryptedPassword);
        newMember.setPasswordChanged(false); // Tandai belum ganti password
        
        // 5. Set Data Spesifik (Child)
        newMember.setExpertDivision(request.getExpertDivision());
        newMember.setDepartment(request.getDepartment());
        
        // 6. Simpan
        return memberRepository.save(newMember);
    }

    @Transactional
    public ResearchAssistant updateMember(String id, UpdateMemberRequest request) {
        // 1. Cari member berdasarkan UUID (bukan NIM)
        ResearchAssistant member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member dengan ID " + id + " tidak ditemukan"));

        // 2. Update field satu per satu (Hanya jika request tidak null/kosong)
        // Ini teknik "Partial Update" biar data lain gak kethimpa jadi null
        
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            member.setFullName(request.getFullName());
        }

        if (request.getExpertDivision() != null && !request.getExpertDivision().isBlank()) {
            member.setExpertDivision(request.getExpertDivision());
        }

        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            member.setDepartment(request.getDepartment());
        }

        // Field Kontak
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) member.setPhoneNumber(request.getPhoneNumber());
        if (request.getSocialMediaLink() != null) member.setSocialMediaLink(request.getSocialMediaLink());

        // 3. Simpan perubahan
        return memberRepository.save(member);
    }

    @Transactional 
    public void deleteMember(String id) {
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member ID " + id + " tidak ditemukan");
        }
        memberRepository.deleteById(id);
    }

    public List<ResearchAssistant> getAllMembers() {
        return memberRepository.findAll();
    }
}
