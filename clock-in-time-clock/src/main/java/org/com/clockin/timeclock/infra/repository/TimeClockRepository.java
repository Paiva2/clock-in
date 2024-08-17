package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TimeClockRepository extends JpaRepository<TimeClock, UUID> {
    @Query(value = """ 
            SELECT COUNT(*) FROM "clock-in-db".public2.tb_time_clocks
            WHERE tc_external_employee_id = :employeeExternalId
            AND date_trunc('day', tc_time_clocked) = date_trunc('day', now())
        """, nativeQuery = true)
    Integer countTimeClocksTodayByEmployee(@Param("employeeExternalId") Long employeeExternalId);
}
