package com.mbclab.lablink.features.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuesPaymentRepository extends JpaRepository<DuesPayment, String> {
    
    List<DuesPayment> findByMemberIdOrderByPaymentYearDescPaymentMonthDesc(String memberId);
    
    List<DuesPayment> findByPeriodId(String periodId);
    
    List<DuesPayment> findByStatus(String status);
    
    List<DuesPayment> findByPeriodIdAndStatus(String periodId, String status);
    
    Optional<DuesPayment> findByMemberIdAndPaymentMonthAndPaymentYear(String memberId, Integer month, Integer year);
    
    @Query("SELECT dp FROM DuesPayment dp WHERE dp.period.id = :periodId AND dp.status = 'UNPAID'")
    List<DuesPayment> findUnpaidByPeriod(String periodId);
    
    @Query("SELECT dp FROM DuesPayment dp WHERE dp.status = 'PENDING' ORDER BY dp.createdAt DESC")
    List<DuesPayment> findPendingVerification();
    
    void deleteByPeriodId(String periodId);
}
