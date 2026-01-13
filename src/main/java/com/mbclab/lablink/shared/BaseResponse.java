package com.mbclab.lablink.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Base Response DTO untuk consistency di semua response.
 * Semua Response DTO bisa extends class ini.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseResponse {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
