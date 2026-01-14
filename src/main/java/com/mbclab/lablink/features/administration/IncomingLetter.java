package com.mbclab.lablink.features.administration;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Entity untuk Surat Masuk.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "incoming_letters")
public class IncomingLetter extends BaseEntity {

    @Column(nullable = false)
    private String referenceNumber;  // Nomor surat dari pengirim

    @Column(nullable = false)
    private String sender;  // Pengirim

    @Column(nullable = false)
    private String subject;  // Perihal

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String attachmentPath;  // Path ke file PDF
}
