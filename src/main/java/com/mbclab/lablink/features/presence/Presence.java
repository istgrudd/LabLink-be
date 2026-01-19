package com.mbclab.lablink.features.presence;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "presence", indexes = {
    @Index(name = "idx_presence_period_id", columnList = "period_id"),
    @Index(name = "idx_presence_date", columnList = "date"),
    @Index(name = "idx_presence_member_id", columnList = "member_id")
})
public class Presence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private ResearchAssistant member;

    // Periode kepengurusan (auto-assigned)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresenceType type;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum PresenceType {
        MEETING,
        ON_CALL,
        OTHER
    }
}
