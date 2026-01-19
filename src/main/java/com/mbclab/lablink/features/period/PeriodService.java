package com.mbclab.lablink.features.period;

import com.mbclab.lablink.features.activitylog.AuditEvent;
import com.mbclab.lablink.features.administration.LetterRepository;
import com.mbclab.lablink.features.archive.ArchiveRepository;
import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.period.dto.*;
import com.mbclab.lablink.features.project.ProjectRepository;
import com.mbclab.lablink.features.event.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mbclab.lablink.config.CacheConfig.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PeriodService {

    private final AcademicPeriodRepository periodRepository;
    private final MemberPeriodRepository memberPeriodRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final EventRepository eventRepository;
    private final ArchiveRepository archiveRepository;
    private final LetterRepository letterRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ========== CREATE ==========
    
    @Transactional
    @CacheEvict(value = {ACTIVE_PERIOD_CACHE, ALL_PERIODS_CACHE}, allEntries = true)
    public PeriodResponse createPeriod(CreatePeriodRequest request) {
        if (periodRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Periode dengan kode " + request.getCode() + " sudah ada");
        }
        
        AcademicPeriod period = new AcademicPeriod();
        period.setCode(request.getCode());
        period.setName(request.getName());
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setActive(false);
        period.setArchived(false);
        
        AcademicPeriod saved = periodRepository.save(period);
        
        eventPublisher.publishEvent(AuditEvent.create(
                "PERIOD", saved.getId(), saved.getName(),
                "Created academic period: " + saved.getCode()));
        
        return toResponse(saved);
    }

    // ========== READ ==========
    
    @Cacheable(value = ALL_PERIODS_CACHE)
    public List<PeriodResponse> getAllPeriods() {
        return periodRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = ACTIVE_PERIOD_CACHE)
    public PeriodResponse getActivePeriod() {
        AcademicPeriod period = periodRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("Tidak ada periode aktif"));
        return toResponse(period);
    }

    public PeriodResponse getPeriodById(String id) {
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        return toResponse(period);
    }

    // ========== ACTIVATE ==========
    
    @Transactional
    public PeriodResponse activatePeriod(String id) {
        // Deactivate current active period first
        periodRepository.findByIsActiveTrue().ifPresent(current -> {
            current.setActive(false);
            periodRepository.save(current);
        });
        
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        
        if (period.isArchived()) {
            throw new RuntimeException("Tidak bisa mengaktifkan periode yang sudah diarsipkan");
        }
        
        period.setActive(true);
        AcademicPeriod saved = periodRepository.save(period);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PERIOD", saved.getId(), saved.getName(),
                "Activated period: " + saved.getCode()));
        
        return toResponse(saved);
    }

    // ========== UPDATE ==========

    @Transactional
    public PeriodResponse updatePeriod(String id, UpdatePeriodRequest request) {
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));

        if (request.getName() != null && !request.getName().isBlank()) {
            period.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            period.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            period.setEndDate(request.getEndDate());
        }

        AcademicPeriod saved = periodRepository.save(period);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PERIOD", saved.getId(), saved.getName(),
                "Updated period: " + saved.getCode()));

        return toResponse(saved);
    }
    
    // ========== ARCHIVE ==========
    
    @Transactional
    public PeriodResponse archivePeriod(String id) {
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        
        if (period.isActive()) {
            // Auto deactivate if archiving
            period.setActive(false);
        }
        
        period.setArchived(true);
        AcademicPeriod saved = periodRepository.save(period);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PERIOD", saved.getId(), saved.getName(),
                "Archived period: " + saved.getCode()));
        
        return toResponse(saved);
    }

    // ========== CLOSE PERIOD ==========
    
    @Transactional
    public PeriodResponse closePeriod(String id, ClosePeriodRequest request) {
        AcademicPeriod oldPeriod = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        
        if (!oldPeriod.isActive()) {
            throw new RuntimeException("Hanya periode aktif yang bisa ditutup");
        }
        
        AcademicPeriod newPeriod = periodRepository.findById(request.getNewPeriodId())
                .orElseThrow(() -> new RuntimeException("Periode baru tidak ditemukan"));
        
        // 1. Get all members in old period
        List<MemberPeriod> oldMembers = memberPeriodRepository.findByPeriodIdAndStatus(id, "ACTIVE");
        
        // 2. Process each member
        for (MemberPeriod mp : oldMembers) {
            if (request.getContinuingMemberIds() != null && 
                request.getContinuingMemberIds().contains(mp.getMember().getId())) {
                // Member continues - add to new period
                MemberPeriod newMp = new MemberPeriod(mp.getMember(), newPeriod, mp.getPosition());
                memberPeriodRepository.save(newMp);
            }
            // Mark as ALUMNI in old period
            mp.setStatus("ALUMNI");
            mp.setGraduatedAt(LocalDateTime.now());
            memberPeriodRepository.save(mp);
        }
        
        // 3. Archive old period
        oldPeriod.setActive(false);
        oldPeriod.setArchived(true);
        periodRepository.save(oldPeriod);
        
        // 4. Activate new period
        newPeriod.setActive(true);
        periodRepository.save(newPeriod);
        
        eventPublisher.publishEvent(AuditEvent.update(
                "PERIOD", oldPeriod.getId(), oldPeriod.getName(),
                "Closed and archived period: " + oldPeriod.getCode()));
        
        return toResponse(oldPeriod);
    }

    // ========== DELETE PERIOD ==========
    
    @Transactional
    public void deletePeriod(String id) {
        AcademicPeriod period = periodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        
        if (period.isActive()) {
            throw new RuntimeException("Tidak bisa menghapus periode yang sedang aktif. Tetapkan periode lain sebagai aktif terlebih dahulu.");
        }
        
        // 1. Delete all member-period associations
        List<MemberPeriod> memberPeriods = memberPeriodRepository.findByPeriodId(id);
        memberPeriodRepository.deleteAll(memberPeriods);
        
        // 2. Delete all archives in this period (must be before projects/events due to FK)
        archiveRepository.deleteByPeriodId(id);
        
        // 3. Delete all letters in this period
        letterRepository.deleteByPeriodId(id);
        
        // 4. Delete all projects in this period
        projectRepository.deleteByPeriodId(id);
        
        // 5. Delete all events in this period
        eventRepository.deleteByPeriodId(id);
        
        // 6. Delete the period itself
        periodRepository.delete(period);
        
        eventPublisher.publishEvent(AuditEvent.delete(
                "PERIOD", period.getId(), period.getName(),
                "Deleted period with cascade: " + period.getCode()));
    }

    // ========== MEMBER MANAGEMENT ==========
    
    @Transactional
    public MemberPeriodResponse addMemberToPeriod(String periodId, AddMemberToPeriodRequest request) {
        AcademicPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Periode tidak ditemukan"));
        
        if (period.isArchived()) {
            throw new RuntimeException("Tidak bisa menambah member ke periode yang sudah diarsipkan");
        }
        
        ResearchAssistant member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        
        // Check if already in period
        MemberPeriodId mpId = new MemberPeriodId(request.getMemberId(), periodId);
        if (memberPeriodRepository.existsById(mpId)) {
            throw new RuntimeException("Member sudah terdaftar di periode ini");
        }
        
        MemberPeriod mp = new MemberPeriod(member, period, request.getPosition());
        MemberPeriod saved = memberPeriodRepository.save(mp);
        
        return toMemberPeriodResponse(saved);
    }

    public List<MemberPeriodResponse> getMembersByPeriod(String periodId) {
        return memberPeriodRepository.findByPeriodId(periodId).stream()
                .map(this::toMemberPeriodResponse)
                .collect(Collectors.toList());
    }

    // ========== HELPERS ==========
    
    private PeriodResponse toResponse(AcademicPeriod period) {
        int totalMembers = memberPeriodRepository.findByPeriodId(period.getId()).size();
        int totalProjects = projectRepository.countByPeriodId(period.getId());
        int totalEvents = eventRepository.countByPeriodId(period.getId());
        
        return PeriodResponse.builder()
                .id(period.getId())
                .code(period.getCode())
                .name(period.getName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .isActive(period.isActive())
                .isArchived(period.isArchived())
                .totalMembers(totalMembers)
                .totalProjects(totalProjects)
                .totalEvents(totalEvents)
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }

    private MemberPeriodResponse toMemberPeriodResponse(MemberPeriod mp) {
        return MemberPeriodResponse.builder()
                .memberId(mp.getMember().getId())
                .memberName(mp.getMember().getFullName())
                .memberNim(mp.getMember().getUsername())
                .periodCode(mp.getPeriod().getCode())
                .status(mp.getStatus())
                .position(mp.getPosition())
                .build();
    }
}
