package com.mbclab.lablink.features.administration.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateIncomingLetterRequest {
    private String referenceNumber;  // Nomor surat dari pengirim (optional)

    @NotBlank(message = "Pengirim surat wajib diisi")
    private String sender;

    @NotBlank(message = "Perihal surat wajib diisi")
    @Size(max = 255, message = "Perihal maksimal 255 karakter")
    private String subject;

    private LocalDate receivedDate;

    @Size(max = 500, message = "Catatan maksimal 500 karakter")
    private String notes;
}
