package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Kategori keuangan dinamis (CRUD oleh Admin).
 * Contoh: Konsumsi, ATK, Equipment, Sponsor, Donasi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "finance_categories", indexes = {
    @Index(name = "idx_category_type", columnList = "type"),
    @Index(name = "idx_category_active", columnList = "isActive")
})
public class FinanceCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * INCOME = Hanya untuk pemasukan
     * EXPENSE = Hanya untuk pengeluaran  
     * BOTH = Bisa untuk keduanya
     */
    @Column(nullable = false)
    private String type = "BOTH";

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean isActive = true;
}
