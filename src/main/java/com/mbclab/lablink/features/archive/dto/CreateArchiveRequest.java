package com.mbclab.lablink.features.archive.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateArchiveRequest {
    private String title;
    private String description;
    private String archiveType;    // PUBLIKASI, HKI, PKM, LAPORAN, SERTIFIKAT
    private String sourceType;     // PROJECT atau EVENT
    private String projectId;      // Set jika sourceType = PROJECT
    private String eventId;        // Set jika sourceType = EVENT
    private String publishLocation;
    private String referenceNumber;
    private LocalDate publishDate;
}
