package com.mbclab.lablink.features.finance.dto;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private String type;        // INCOME, EXPENSE, BOTH
    private String description;
}
