package com.mbclab.lablink.features.period;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberPeriodRepository extends JpaRepository<MemberPeriod, MemberPeriodId> {
    List<MemberPeriod> findByPeriodId(String periodId);
    List<MemberPeriod> findByMemberId(String memberId);
    List<MemberPeriod> findByPeriodIdAndStatus(String periodId, String status);
}
