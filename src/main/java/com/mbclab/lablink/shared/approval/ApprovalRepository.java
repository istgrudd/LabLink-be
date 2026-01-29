package com.mbclab.lablink.shared.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * Base repository interface untuk entity dengan approval workflow.
 * 
 * @param <E> Entity type yang implements Approvable
 * @param <ID> ID type (biasanya String)
 */
@NoRepositoryBean
public interface ApprovalRepository<E extends Approvable, ID> extends JpaRepository<E, ID> {
    
    /**
     * Find all entities dengan approval status tertentu.
     */
    List<E> findByApprovalStatus(String approvalStatus);
}
