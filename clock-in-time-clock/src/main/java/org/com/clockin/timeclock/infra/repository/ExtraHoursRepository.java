package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtraHoursRepository extends JpaRepository<ExtraHours, UUID> {
    Optional<ExtraHours> findByDayPeriodAndExternalEmployeeId(String dayPeriod, Long externalEmployeeId);

    @Query(value = """
            SELECT * FROM "clock-in-db".public2.tb_extra_hours
            WHERE eh_external_employee_id = :employeeId
            AND eh_extra_hours <> '00H00M'
            AND (:period IS NULL OR eh_day_period = :period)
        """, nativeQuery = true)
    Page<ExtraHours> findAllByExternalEmployeeId(@Param("employeeId") Long employeeId, @Param("period") String period, Pageable pageable);

    @Query(value = """
           SELECT SUM( EXTRACT (EPOCH FROM eh_extra_hours::interval) )
           FROM "clock-in-db".public2.tb_extra_hours
           WHERE eh_external_employee_id = :employeeId
           AND eh_extra_hours <> '00H00M'
           AND (:period IS NULL OR eh_day_period = :period)
        """, nativeQuery = true)
    Long findTotalByExternalEmployeeId(@Param("employeeId") Long employeeId, @Param("period") String period);
}
