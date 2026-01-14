package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String eventCode;  // EVT-0001

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status = "PLANNED";  // PLANNED, ONGOING, COMPLETED, CANCELLED

    // Periode kepengurusan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    // Person In Charge (Ketua Pelaksana)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pic_id", nullable = false)
    private ResearchAssistant pic;

    // Committee members (dengan role)
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventCommittee> committee = new HashSet<>();
}
