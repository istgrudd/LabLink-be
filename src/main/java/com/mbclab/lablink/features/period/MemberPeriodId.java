package com.mbclab.lablink.features.period;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key untuk MemberPeriod.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPeriodId implements Serializable {
    private String member;  // ID dari ResearchAssistant
    private String period;  // ID dari AcademicPeriod
}
