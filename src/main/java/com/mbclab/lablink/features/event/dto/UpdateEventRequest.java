package com.mbclab.lablink.features.event.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEventRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // PLANNED, ONGOING, COMPLETED, CANCELLED
    private String picId;
    private java.util.List<CommitteeMemberRequest> committee;
    
    @Data
    public static class CommitteeMemberRequest {
        private String memberId;
        private String role;
    }
}
