package com.mbclab.lablink.features.presence.dto;

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
public class PresenceResponse extends BaseResponse {
    
    private String memberName;
    private String type;
    private LocalDate date;
    private String title;
    private String imageUrl; // Full URL to access image
    private String notes;
}
