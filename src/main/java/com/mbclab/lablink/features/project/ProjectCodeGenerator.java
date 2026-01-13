package com.mbclab.lablink.features.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service untuk generate project code berdasarkan kategori.
 * Format: RST-0001 (Riset), HKI-0001, PKM-0001 (Pengabdian)
 */
@Service
@RequiredArgsConstructor
public class ProjectCodeGenerator {

    private final ProjectRepository projectRepository;

    public String generate(String activityType) {
        String prefix = switch (activityType.toUpperCase()) {
            case "RISET" -> "RST";
            case "HKI" -> "HKI";
            case "PENGABDIAN" -> "PKM";
            default -> "PRJ";
        };

        long count = projectRepository.countByActivityType(activityType.toUpperCase());
        return String.format("%s-%04d", prefix, count + 1);
    }
}
