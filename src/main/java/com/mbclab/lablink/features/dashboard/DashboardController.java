package com.mbclab.lablink.features.dashboard;

import com.mbclab.lablink.features.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardResponse> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
