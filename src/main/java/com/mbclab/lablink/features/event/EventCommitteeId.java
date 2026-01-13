package com.mbclab.lablink.features.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite Key untuk EventCommittee (Event ID + Member ID)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EventCommitteeId implements Serializable {
    
    @Column(name = "event_id")
    private String eventId;
    
    @Column(name = "member_id")
    private String memberId;
}
