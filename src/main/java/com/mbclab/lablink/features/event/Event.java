package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.shared.BaseEntity;
import com.mbclab.lablink.shared.approval.Approvable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_period_id", columnList = "period_id"),
    @Index(name = "idx_event_start_date", columnList = "startDate"),
    @Index(name = "idx_event_status", columnList = "status"),
    @Index(name = "idx_event_approval_status", columnList = "approvalStatus")
})
public class Event extends BaseEntity implements Approvable {

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

    // ========== APPROVAL WORKFLOW ==========

    // Status approval: PENDING, APPROVED, REJECTED
    @Column(nullable = false)
    private String approvalStatus = "PENDING";

    // Alasan penolakan (jika REJECTED)
    private String rejectionReason;

    // Tanggal disetujui
    private LocalDate approvedAt;

    // User yang approve/reject
    private String approvedBy;

    // ========== RELASI ==========

    // Periode kepengurusan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private AcademicPeriod period;

    // Person In Charge (Ketua Pelaksana)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pic_id", nullable = false)
    private ResearchAssistant pic;

    // Committee members (dengan role)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventCommittee> committee = new HashSet<>();

    // Event schedules (untuk calendar)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventSchedule> schedules = new HashSet<>();

    // ========== APPROVABLE INTERFACE ==========
    
    @Override
    public String getDisplayName() {
        return this.name;
    }
}
