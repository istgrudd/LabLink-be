package com.mbclab.lablink.features.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Nama proyek wajib diisi")
    @Size(min = 3, max = 200, message = "Nama proyek harus 3-200 karakter")
    private String name;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    @NotBlank(message = "Divisi wajib dipilih")
    private String division;      // CYBER_SECURITY, BIG_DATA, GIS, GAME_TECH, CROSS_DIVISION

    @NotBlank(message = "Tipe aktivitas wajib dipilih")
    private String activityType;  // RISET, HKI, PENGABDIAN

    @NotBlank(message = "Leader ID wajib diisi")
    private String leaderId;      // ID Ketua Proyek

    private Set<String> teamMemberIds;  // ID Anggota Tim (optional)

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    private LocalDate endDate;
}
