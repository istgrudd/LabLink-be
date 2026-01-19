package com.mbclab.lablink.features.finance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinanceTransactionRepository extends JpaRepository<FinanceTransaction, String> {
    
    Page<FinanceTransaction> findByType(String type, Pageable pageable);
    
    List<FinanceTransaction> findByCategoryId(String categoryId);
    
    List<FinanceTransaction> findByTransactionDateBetween(LocalDate start, LocalDate end);
    
    List<FinanceTransaction> findByEventId(String eventId);
    
    List<FinanceTransaction> findByProjectId(String projectId);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinanceTransaction t WHERE t.type = 'INCOME'")
    BigDecimal getTotalIncome();
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinanceTransaction t WHERE t.type = 'EXPENSE'")
    BigDecimal getTotalExpense();
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinanceTransaction t WHERE t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal getTotalByTypeAndDateRange(String type, LocalDate start, LocalDate end);
    
    @Query("SELECT t.category.name, SUM(t.amount) FROM FinanceTransaction t WHERE t.type = :type GROUP BY t.category.name")
    List<Object[]> getSummaryByCategory(String type);
    
    void deleteByPeriodId(String periodId);
}
