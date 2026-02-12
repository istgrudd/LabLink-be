package com.mbclab.lablink.features.event.dto;

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
public class EventResponse extends BaseResponse {

    private String eventCode;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    
    // ========== APPROVAL WORKFLOW ==========
    private String approvalStatus;      // PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private LocalDate approvedAt;
    private String approvedBy;
    
    // PIC summary
    private MemberSummary pic;
    
    // Committee members
    private List<CommitteeMember> committee;
    
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
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommitteeMember {
        private String memberId;
        private String username;
        private String fullName;
        private String role;
    }
}
