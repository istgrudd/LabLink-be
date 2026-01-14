package com.mbclab.lablink.features.archive.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateArchiveRequest {
    private String title;
    private String description;
    private String publishLocation;
    private String referenceNumber;
    private LocalDate publishDate;
}
