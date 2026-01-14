package com.mbclab.lablink.features.activitylog.dto;

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
public class ActivityLogResponse extends BaseResponse {
    private String action;
    private String targetType;
    private String targetId;
    private String targetName;
    private String description;
    private String userId;
    private String userName;
    private String ipAddress;
}
