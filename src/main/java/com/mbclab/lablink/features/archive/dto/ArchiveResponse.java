package com.mbclab.lablink.features.archive.dto;

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
public class ArchiveResponse extends BaseResponse {

    private String archiveCode;
    private String title;
    private String description;
    private String archiveType;
    private String department;
    private String sourceType;
    
    // Source info
    private SourceInfo source;
    
    private String publishLocation;
    private String referenceNumber;
    private LocalDate publishDate;
    
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        private String id;
        private String code;     // projectCode atau eventCode
        private String name;
        private String leader;   // Nama leader/PIC
    }
}
