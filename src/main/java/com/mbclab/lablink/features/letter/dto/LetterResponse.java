package com.mbclab.lablink.features.letter.dto;

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
    private LocalDate issueDate;
    private String status;
    private String createdBy;
    
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
