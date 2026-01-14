package com.mbclab.lablink.features.archive;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generate Archive Code berdasarkan tipe arsip.
 * 
 * Project types:
 * - PUBLIKASI → PUB-0001
 * - HKI → HKI-0001
 * - PKM → PKM-0001
 * 
 * Event types:
 * - LAPORAN → LPR-0001
 * - SERTIFIKAT → SRT-0001
 */
@Service
@RequiredArgsConstructor
public class ArchiveCodeGenerator {

    private final ArchiveRepository archiveRepository;

    public String generate(String archiveType) {
        String prefix = getPrefix(archiveType);
        long count = archiveRepository.countByArchiveCodeStartingWith(prefix) + 1;
        return String.format("%s-%04d", prefix, count);
    }

    private String getPrefix(String archiveType) {
        return switch (archiveType.toUpperCase()) {
            case "PUBLIKASI" -> "PUB";
            case "HKI" -> "HKI";
            case "PKM" -> "PKM";
            case "LAPORAN" -> "LPR";
            case "SERTIFIKAT" -> "SRT";
            default -> "ARC";
        };
    }
}
