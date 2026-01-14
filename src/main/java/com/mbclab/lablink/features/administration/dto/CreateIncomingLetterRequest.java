package com.mbclab.lablink.features.administration.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateIncomingLetterRequest {
    private String referenceNumber;  // Nomor surat dari pengirim
    private String sender;           // Pengirim
    private String subject;          // Perihal
    private LocalDate receivedDate;
    private String notes;
}
