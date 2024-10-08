package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
            SELECT pua FROM PendingUpdateApproval pua
            JOIN FETCH pua.timeClock tc
            WHERE pua.id = :pendingUpdateId
        """)
    Optional<PendingUpdateApproval> findByIdWithDeps(@Param("pendingUpdateId") UUID pendingUpdateId);

    @Query(value = """
            SELECT pua FROM PendingUpdateApproval pua
            JOIN FETCH pua.timeClock tc
            WHERE pua.approved IS NULL
            AND tc.externalEmployeeId = :employeeId
            ORDER BY pua.timeClockUpdated ASC
        """)
    Page<PendingUpdateApproval> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    List<PendingUpdateApproval> findAllByTimeClockId(UUID timeClockId);

    void deleteAllByTimeClockId(UUID timeClockId);
}
