package com.mbclab.lablink.features.event;

import com.mbclab.lablink.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity untuk jadwal kegiatan detail per tanggal dalam suatu Event.
 * Digunakan untuk calendar view.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "event_schedules", indexes = {
    @Index(name = "idx_schedule_activity_date", columnList = "activityDate"),
    @Index(name = "idx_schedule_event_id", columnList = "event_id")
})
public class EventSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private LocalDate activityDate;  // Tanggal kegiatan spesifik

    @Column(nullable = false)
    private String title;  // Nama kegiatan (e.g., "Opening Ceremony")

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalTime startTime;  // Jam mulai (opsional)
    private LocalTime endTime;    // Jam selesai (opsional)

    private String location;      // Lokasi spesifik (opsional)
}
