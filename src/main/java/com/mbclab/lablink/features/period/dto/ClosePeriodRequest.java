package com.mbclab.lablink.features.period.dto;

import lombok.Data;
import java.util.Set;

@Data
public class ClosePeriodRequest {
    private String newPeriodId;       // ID periode baru yang akan active
    private Set<String> continuingMemberIds;  // Member yang lanjut ke periode baru
}
