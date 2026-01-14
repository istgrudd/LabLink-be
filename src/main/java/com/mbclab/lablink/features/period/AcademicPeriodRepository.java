package com.mbclab.lablink.features.period;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, String> {
    Optional<AcademicPeriod> findByCode(String code);
    Optional<AcademicPeriod> findByIsActiveTrue();
}
