package com.mbclab.lablink.features.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRoleRepository extends JpaRepository<MemberRole, String> {
    
    List<MemberRole> findByMemberId(String memberId);
    
    Optional<MemberRole> findByMemberIdAndRole(String memberId, Role role);
    
    boolean existsByMemberIdAndRole(String memberId, Role role);
    
    void deleteByMemberIdAndRole(String memberId, Role role);
    
    void deleteByMemberId(String memberId);
    
    List<MemberRole> findByRole(Role role);
}
