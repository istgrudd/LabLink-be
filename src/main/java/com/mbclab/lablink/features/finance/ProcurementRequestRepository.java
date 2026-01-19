package com.mbclab.lablink.features.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementRequestRepository extends JpaRepository<ProcurementRequest, String> {
    
    List<ProcurementRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId);
    
    Page<ProcurementRequest> findByStatus(String status, Pageable pageable);
    
    List<ProcurementRequest> findByStatusOrderByPriorityDescCreatedAtAsc(String status);
    
    List<ProcurementRequest> findByStatusIn(List<String> statuses);
    
    long countByStatus(String status);
}
