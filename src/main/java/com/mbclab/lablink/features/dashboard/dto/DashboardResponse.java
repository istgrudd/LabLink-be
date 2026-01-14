package com.mbclab.lablink.features.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Statistics statistics;
    private List<UpcomingItem> upcomingDeadlines;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private int totalProjects;
        private int activeProjects;
        private int completedProjects;
        
        private int totalEvents;
        private int ongoingEvents;
        private int completedEvents;
        
        private int totalMembers;
        private int activeMembers;
        
        private int totalArchives;
        private int totalLetters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingItem {
        private String type;       // PROJECT, EVENT
        private String id;
        private String code;
        private String name;
        private LocalDate deadline;
        private int daysRemaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private String action;
        private String targetType;
        private String targetName;
        private String userName;
        private LocalDateTime timestamp;
        private String timeAgo;
    }
}
