package com.mbclab.lablink.features.period;

import com.mbclab.lablink.features.member.ResearchAssistant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Junction table untuk relasi Member-Period.
 * Member bisa aktif di beberapa periode dengan jabatan berbeda.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "member_periods")
@IdClass(MemberPeriodId.class)
public class MemberPeriod {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private ResearchAssistant member;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    // Status dalam periode ini: ACTIVE, ALUMNI
    @Column(nullable = false)
    private String status = "ACTIVE";

    // Jabatan dalam periode ini
    private String position;

    // Tanggal bergabung di periode ini
    private LocalDateTime joinedAt;

    // Tanggal menjadi alumni (jika ada)
    private LocalDateTime graduatedAt;

    public MemberPeriod(ResearchAssistant member, AcademicPeriod period, String position) {
        this.member = member;
        this.period = period;
        this.position = position;
        this.status = "ACTIVE";
        this.joinedAt = LocalDateTime.now();
    }
}
