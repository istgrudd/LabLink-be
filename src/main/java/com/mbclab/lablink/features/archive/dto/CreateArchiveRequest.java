package com.mbclab.lablink.features.archive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateArchiveRequest {

    @NotBlank(message = "Judul arsip wajib diisi")
    @Size(min = 3, max = 200, message = "Judul harus 3-200 karakter")
    private String title;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    @NotBlank(message = "Tipe arsip wajib dipilih")
    private String archiveType;    // PUBLIKASI, HKI, PKM, LAPORAN, SERTIFIKAT

    @NotBlank(message = "Source type wajib dipilih")
    private String sourceType;     // PROJECT atau EVENT

    private String projectId;      // Set jika sourceType = PROJECT
    private String eventId;        // Set jika sourceType = EVENT

    @Size(max = 200, message = "Lokasi publikasi maksimal 200 karakter")
    private String publishLocation;

    @Size(max = 100, message = "Nomor referensi maksimal 100 karakter")
    private String referenceNumber;

    private LocalDate publishDate;
}
