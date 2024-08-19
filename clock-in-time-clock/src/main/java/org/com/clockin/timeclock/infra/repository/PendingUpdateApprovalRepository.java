package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PendingUpdateApprovalRepository extends JpaRepository<PendingUpdateApproval, UUID> {
}
