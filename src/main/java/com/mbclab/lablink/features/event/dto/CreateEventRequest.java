package com.mbclab.lablink.features.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEventRequest {

    @NotBlank(message = "Nama event wajib diisi")
    @Size(min = 3, max = 200, message = "Nama event harus 3-200 karakter")
    private String name;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "PIC ID wajib diisi")
    private String picId;  // Person In Charge ID
    
    // Optional: Create schedules directly
    @Valid
    private List<EventScheduleRequest> schedules;
}
