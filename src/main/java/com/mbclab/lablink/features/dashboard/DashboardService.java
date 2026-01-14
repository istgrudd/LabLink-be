package com.mbclab.lablink.features.dashboard;

import com.mbclab.lablink.features.activitylog.ActivityLog;
import com.mbclab.lablink.features.activitylog.ActivityLogRepository;
import com.mbclab.lablink.features.archive.ArchiveRepository;
import com.mbclab.lablink.features.dashboard.dto.DashboardResponse;
import com.mbclab.lablink.features.event.Event;
import com.mbclab.lablink.features.event.EventRepository;
import com.mbclab.lablink.features.administration.LetterRepository;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.period.AcademicPeriod;
import com.mbclab.lablink.features.period.AcademicPeriodRepository;
import com.mbclab.lablink.features.period.MemberPeriodRepository;
import com.mbclab.lablink.features.project.Project;
import com.mbclab.lablink.features.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ArchiveRepository archiveRepository;
    private final LetterRepository letterRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AcademicPeriodRepository periodRepository;
    private final MemberPeriodRepository memberPeriodRepository;

    public DashboardResponse getDashboardSummary() {
        // Get active period
        AcademicPeriod activePeriod = periodRepository.findByIsActiveTrue().orElse(null);
        
        return DashboardResponse.builder()
                .statistics(buildStatistics(activePeriod))
                .upcomingDeadlines(buildUpcomingDeadlines(activePeriod))
                .recentActivities(buildRecentActivities())
                .build();
    }

    private DashboardResponse.Statistics buildStatistics(AcademicPeriod activePeriod) {
        List<Project> allProjects;
        List<Event> allEvents;
        int activeMembers;
        
        if (activePeriod != null) {
            allProjects = projectRepository.findByPeriodId(activePeriod.getId());
            allEvents = eventRepository.findByPeriodId(activePeriod.getId());
            activeMembers = memberPeriodRepository.findByPeriodIdAndStatus(
                    activePeriod.getId(), "ACTIVE").size();
        } else {
            allProjects = projectRepository.findAll();
            allEvents = eventRepository.findAll();
            activeMembers = (int) memberRepository.count();
        }
        
        int activeProjects = (int) allProjects.stream()
                .filter(p -> "IN_PROGRESS".equals(p.getStatus()))
                .count();
        int completedProjects = (int) allProjects.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        
        int ongoingEvents = (int) allEvents.stream()
                .filter(e -> "ONGOING".equals(e.getStatus()))
                .count();
        int completedEvents = (int) allEvents.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();
        
        return DashboardResponse.Statistics.builder()
                .totalProjects(allProjects.size())
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .totalEvents(allEvents.size())
                .ongoingEvents(ongoingEvents)
                .completedEvents(completedEvents)
                .totalMembers((int) memberRepository.count())
                .activeMembers(activeMembers)
                .totalArchives((int) archiveRepository.count())
                .totalLetters((int) letterRepository.count())
                .build();
    }

    private List<DashboardResponse.UpcomingItem> buildUpcomingDeadlines(AcademicPeriod activePeriod) {
        List<DashboardResponse.UpcomingItem> items = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusDays(30);
        
        // Get projects with upcoming deadlines
        List<Project> projects;
        if (activePeriod != null) {
            projects = projectRepository.findByPeriodId(activePeriod.getId());
        } else {
            projects = projectRepository.findAll();
        }
        
        projects.stream()
                .filter(p -> p.getEndDate() != null)
                .filter(p -> !p.getEndDate().isBefore(today))
                .filter(p -> p.getEndDate().isBefore(nextMonth))
                .filter(p -> !"COMPLETED".equals(p.getStatus()) && !"CANCELLED".equals(p.getStatus()))
                .forEach(project -> items.add(DashboardResponse.UpcomingItem.builder()
                        .type("PROJECT")
                        .id(project.getId())
                        .code(project.getProjectCode())
                        .name(project.getName())
                        .deadline(project.getEndDate())
                        .daysRemaining((int) ChronoUnit.DAYS.between(today, project.getEndDate()))
                        .build()));
        
        // Get events starting soon
        List<Event> events;
        if (activePeriod != null) {
            events = eventRepository.findByPeriodId(activePeriod.getId());
        } else {
            events = eventRepository.findAll();
        }
        
        events.stream()
                .filter(e -> e.getStartDate() != null)
                .filter(e -> !e.getStartDate().isBefore(today))
                .filter(e -> e.getStartDate().isBefore(nextMonth))
                .filter(e -> "PLANNED".equals(e.getStatus()))
                .forEach(event -> items.add(DashboardResponse.UpcomingItem.builder()
                        .type("EVENT")
                        .id(event.getId())
                        .code(event.getEventCode())
                        .name(event.getName())
                        .deadline(event.getStartDate())
                        .daysRemaining((int) ChronoUnit.DAYS.between(today, event.getStartDate()))
                        .build()));
        
        // Sort by deadline and limit to 10
        return items.stream()
                .sorted(Comparator.comparing(DashboardResponse.UpcomingItem::getDaysRemaining))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DashboardResponse.RecentActivity> buildRecentActivities() {
        return activityLogRepository.findAll(
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        ).stream()
                .map(this::toRecentActivity)
                .collect(Collectors.toList());
    }

    private DashboardResponse.RecentActivity toRecentActivity(ActivityLog log) {
        return DashboardResponse.RecentActivity.builder()
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetName(log.getTargetName())
                .userName(log.getUserName())
                .timestamp(log.getCreatedAt())
                .timeAgo(getTimeAgo(log.getCreatedAt()))
                .build();
    }

    private String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) return "baru saja";
        if (minutes < 60) return minutes + " menit lalu";
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) return hours + " jam lalu";
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) return days + " hari lalu";
        
        return dateTime.toLocalDate().toString();
    }
}
