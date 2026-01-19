package com.mbclab.lablink.features.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Role enum dengan display name dan description untuk frontend.
 */
@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("Administrator", "Full access ke semua fitur sistem"),
    SECRETARY("Sekretaris", "Manage dan approve pengajuan surat"),
    TREASURER("Bendahara", "Manage dan approve transaksi keuangan"),
    RESEARCH_COORD("Koordinator Riset", "Approve semua project riset/HKI/PKM"),
    DIVISION_HEAD("Kepala Divisi", "Approve project dalam divisi masing-masing"),
    TECH_OPS("Technical Operation", "Manage archive dan maintenance sistem"),
    ASSISTANT("Asisten", "Akses dasar untuk asisten biasa");

    private final String displayName;
    private final String description;
}
