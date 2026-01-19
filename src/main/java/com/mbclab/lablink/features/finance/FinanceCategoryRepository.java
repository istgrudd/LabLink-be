package com.mbclab.lablink.features.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, String> {
    
    List<FinanceCategory> findByIsActiveTrue();
    
    List<FinanceCategory> findByType(String type);
    
    List<FinanceCategory> findByTypeAndIsActiveTrue(String type);
    
    Optional<FinanceCategory> findByName(String name);
    
    boolean existsByName(String name);
}
