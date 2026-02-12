package com.mbclab.lablink.features.administration.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLetterRequest {
    @NotBlank(message = "Jenis surat wajib diisi")
    private String letterType;    // PMJ, IZN, STF, SP, UND

    @NotBlank(message = "Kategori surat wajib diisi")
    private String category;      // RK, INT, EXT, WSH

    @NotBlank(message = "Perihal surat wajib diisi")
    @Size(max = 255, message = "Perihal maksimal 255 karakter")
    private String subject;

    @NotBlank(message = "Tujuan surat wajib diisi")
    private String recipient;

    @Size(max = 2000, message = "Isi surat maksimal 2000 karakter")
    private String content;       // Optional

    private String attachment;    // Optional

    // Event terkait (for nama_kegiatan)
    private String eventId;

    // Borrow date/time (for PMJ letters)
    private LocalDate borrowDate;
    private LocalDate borrowReturnDate;
}
