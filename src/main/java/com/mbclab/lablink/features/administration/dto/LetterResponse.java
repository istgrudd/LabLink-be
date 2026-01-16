package com.mbclab.lablink.features.administration.dto;

import com.mbclab.lablink.shared.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LetterResponse extends BaseResponse {

    private String letterNumber;
    private String letterType;
    private String category;
    private String subject;
    private String recipient;
    private String content;
    private String attachment;
    
    // Requester info
    private String requesterName;
    private String requesterNim;
    
    // Borrow date/return date
    private LocalDate borrowDate;
    private LocalDate borrowReturnDate;
    
    // Dates
    private LocalDate issueDate;  // Set on approval
    
    // Status: PENDING, APPROVED, REJECTED, DOWNLOADED
    private String status;
    
    // Approval info
    private String approvedBy;
    private String rejectionReason;
    
    // Event info (if linked)
    private EventSummary event;
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSummary {
        private String id;
        private String eventCode;
        private String name;
    }
}
