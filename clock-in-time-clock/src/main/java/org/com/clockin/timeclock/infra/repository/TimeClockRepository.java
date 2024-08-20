package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeClockRepository extends JpaRepository<TimeClock, UUID> {
    @Query(value = """ 
            SELECT * FROM "clock-in-db".public2.tb_time_clocks
            WHERE tc_external_employee_id = :employeeExternalId
            AND date_trunc('day', tc_time_clocked) = date_trunc('day', cast(:dayTime as DATE))
        """, nativeQuery = true)
    List<TimeClock> getTimeClocksOnDayByEmployee(@Param("employeeExternalId") Long employeeExternalId, @Param("dayTime") Date dayTime);

    @Query(value = """
        SELECT * FROM "clock-in-db".public2.tb_time_clocks
        WHERE tc_external_employee_id = :employeeExternalId
        AND ((date_trunc('day', tc_time_clocked) >= :startDate AND (date_trunc('day', tc_time_clocked) <= :endDate)))
        """, nativeQuery = true)
    Page<TimeClock> findAllByEmployee(@Param("employeeExternalId") Long employeeExternalId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    Optional<TimeClock> findByExternalEmployeeIdAndId(Long employeeId, UUID id);
}
