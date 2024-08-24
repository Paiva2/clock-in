package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingUpdateApprovalRepository extends JpaRepository<PendingUpdateApproval, UUID> {
    @Query(value = """
            SELECT * FROM "clock-in-db".public2.tb_pending_update_approvals pua
            INNER JOIN "clock-in-db".public2.tb_time_clocks tc ON tc.tc_id = pua.pua_time_clock_id
            WHERE pua.pua_id = :pendingUpdateId
            AND tc.tc_external_employee_id = :externalEmployeeId
        """, nativeQuery = true)
    Optional<PendingUpdateApproval> findByIdAndEmployeeId(@Param("pendingUpdateId") UUID pendingUpdateId, @Param("externalEmployeeId") Long externalEmployeeId);

    void deleteAllByTimeClockId(UUID timeClockId);
}
