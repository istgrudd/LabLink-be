package com.mbclab.lablink.features.project.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectResponse extends BaseResponse {

    private String projectCode;
    private String name;
    private String description;
    private String division;
    private String activityType;
    private String status;
    private Integer progressPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Leader summary
    private MemberSummary leader;
    
    // Team members summary
    private List<MemberSummary> teamMembers;
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummary {
        private String id;
        private String username;
        private String fullName;
        private String expertDivision;
    }
}
