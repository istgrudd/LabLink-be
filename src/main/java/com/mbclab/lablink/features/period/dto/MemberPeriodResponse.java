package com.mbclab.lablink.features.period.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemberPeriodResponse extends BaseResponse {
    private String memberId;
    private String memberName;
    private String memberNim;
    private String periodCode;
    private String status;
    private String position;
}
