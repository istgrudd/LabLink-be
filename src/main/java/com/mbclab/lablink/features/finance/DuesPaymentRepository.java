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
    
    @EntityGraph(attributePaths = {"member"})
    List<DuesPayment> findByMemberIdOrderByPaymentYearDescPaymentMonthDesc(String memberId);
    
    List<DuesPayment> findByStatus(PaymentStatus status);
    
    Optional<DuesPayment> findByMemberIdAndPaymentMonthAndPaymentYear(String memberId, Integer month, Integer year);
    
    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT dp FROM DuesPayment dp WHERE dp.status = 'PENDING' ORDER BY dp.createdAt DESC")
    List<DuesPayment> findPendingVerification();

    // Paginated version for admin listing
    @EntityGraph(attributePaths = {"member"})
    Page<DuesPayment> findAll(Pageable pageable);
}
