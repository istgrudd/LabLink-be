package com.mbclab.lablink.shared;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    private String id;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
