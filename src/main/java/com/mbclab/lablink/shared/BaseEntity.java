package com.mbclab.lablink.shared;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // 1. Generate ID otomatis (Random String Unik)
    private String id;
    
    @CreationTimestamp // 2. Otomatis isi jam saat pertama kali INSERT
    @Column(updatable = false) // Menjaga agar tanggal pembuatan tidak bisa diedit
    private LocalDateTime createdAt;
    
    @UpdateTimestamp // 3. Otomatis update jam setiap kali ada UPDATE
    private LocalDateTime updatedAt;
}