package com.mbclab.lablink.features.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<ResearchAssistant, String> {
    // Kita butuh cek apakah NIM sudah ada biar gak duplikat
    boolean existsByUsername(String username);
    
    // Cari member berdasarkan NIM (Username)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "memberRoles")
    Optional<ResearchAssistant> findByUsername(String username);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "memberRoles")
    Optional<ResearchAssistant> findById(String id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "memberRoles")
    org.springframework.data.domain.Page<ResearchAssistant> findAll(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "memberRoles")
    java.util.List<ResearchAssistant> findAll();
}
