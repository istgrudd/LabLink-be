package com.mbclab.lablink.features.administration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Generate Letter Number dengan format: 001/PMJ/EXT/MBC/XII/2025
 * 
 * Components:
 * - 001: Nomor urut (per jenis, kategori, bulan, tahun)
 * - PMJ: Jenis surat
 * - EXT: Kategori
 * - MBC: Organisasi (fixed)
 * - XII: Bulan (Romawi)
 * - 2025: Tahun
 */
@Service
@RequiredArgsConstructor
public class LetterNumberGenerator {

    private final LetterRepository letterRepository;

    private static final String[] ROMAN_MONTHS = {
        "", "I", "II", "III", "IV", "V", "VI", 
        "VII", "VIII", "IX", "X", "XI", "XII"
    };

    public String generate(String letterType, String category, LocalDate issueDate) {
        int year = issueDate.getYear();
        int month = issueDate.getMonthValue();
        
        // Get sequence number
        long count = letterRepository.countByTypeAndCategoryAndYearAndMonth(
                letterType, category, year, month) + 1;
        
        // Format: 001/PMJ/EXT/MBC/XII/2025
        return String.format("%03d/%s/%s/MBC/%s/%d",
                count,
                letterType.toUpperCase(),
                category.toUpperCase(),
                ROMAN_MONTHS[month],
                year);
    }
}
