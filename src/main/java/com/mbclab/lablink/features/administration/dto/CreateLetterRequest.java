package com.mbclab.lablink.features.administration.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLetterRequest {
    private String letterType;    // PMJ, IZN, STF, SP, UND
    private String category;      // RK, INT, EXT, WSH
    private String subject;       // Perihal
    private String recipient;     // Tujuan
    private String content;       // Isi surat (optional)
    private String attachment;    // Lampiran (optional)
    private LocalDate issueDate;  // Tanggal surat
    private String eventId;       // Optional: Link ke Event
    private String createdBy;     // Pembuat surat
}
