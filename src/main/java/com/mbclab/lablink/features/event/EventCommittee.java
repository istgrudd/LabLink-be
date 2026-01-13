package com.mbclab.lablink.features.event;

import com.mbclab.lablink.features.member.ResearchAssistant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Join Entity untuk Event Committee dengan role tambahan.
 * Ini diperlukan karena @ManyToMany biasa tidak support field tambahan.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "event_committee")
public class EventCommittee {

    @EmbeddedId
    private EventCommitteeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private ResearchAssistant member;

    @Column(nullable = false)
    private String role;  // "Humas", "Acara", "Bendahara", dll

    public EventCommittee(Event event, ResearchAssistant member, String role) {
        this.event = event;
        this.member = member;
        this.role = role;
        this.id = new EventCommitteeId(event.getId(), member.getId());
    }
}
