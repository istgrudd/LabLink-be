package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.shared.status.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuesPaymentRepository extends JpaRepository<DuesPayment, String> {
    
    @EntityGraph(attributePaths = {"member", "period"})
    List<DuesPayment> findByMemberIdOrderByPaymentYearDescPaymentMonthDesc(String memberId);
    
    List<DuesPayment> findByPeriodId(String periodId);
    
    List<DuesPayment> findByStatus(PaymentStatus status);
    
    List<DuesPayment> findByPeriodIdAndStatus(String periodId, PaymentStatus status);
    
    Optional<DuesPayment> findByMemberIdAndPaymentMonthAndPaymentYear(String memberId, Integer month, Integer year);
    
    @Query("SELECT dp FROM DuesPayment dp WHERE dp.period.id = :periodId AND dp.status = 'UNPAID'")
    List<DuesPayment> findUnpaidByPeriod(String periodId);
    
    @EntityGraph(attributePaths = {"member", "period"})
    @Query("SELECT dp FROM DuesPayment dp WHERE dp.status = 'PENDING' ORDER BY dp.createdAt DESC")
    List<DuesPayment> findPendingVerification();

    // Paginated version for admin listing
    @EntityGraph(attributePaths = {"member", "period"})
    Page<DuesPayment> findAll(Pageable pageable);
    
    void deleteByPeriodId(String periodId);
}
