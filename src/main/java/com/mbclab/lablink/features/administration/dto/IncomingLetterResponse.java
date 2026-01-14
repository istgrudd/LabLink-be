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
public class IncomingLetterResponse extends BaseResponse {
    private String referenceNumber;
    private String sender;
    private String subject;
    private LocalDate receivedDate;
    private String notes;
    private String attachmentPath;
}
