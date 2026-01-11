package com.mbclab.lablink.features.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<ResearchAssistant, String> {
    // Kita butuh cek apakah NIM sudah ada biar gak duplikat
    boolean existsByUsername(String username);
    
    // Cari member berdasarkan NIM (Username)
    Optional<ResearchAssistant> findByUsername(String username);
}
