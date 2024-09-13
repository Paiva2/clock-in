package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtraHoursRepository extends JpaRepository<ExtraHours, UUID> {
    Optional<ExtraHours> findByDayPeriodAndExternalEmployeeId(String dayPeriod, Long externalEmployeeId);

    @Query(value = """
            SELECT * FROM "clock-in-db".public2.tb_extra_hours
            WHERE eh_external_employee_id = :employeeId
            AND eh_extra_hours <> '00H00M'
            AND ( (:period IS NULL) OR eh_day_period = :period )
            AND ( (:from IS NULL AND :to IS NULL) OR
                    (
                        (:from IS NOT NULL AND :to IS NOT NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') BETWEEN to_char(to_date(:from, 'DD-MM-YYYY'), 'YYYY-MM-DD') AND to_char(to_date(:to, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                        OR
                        (:from IS NOT NULL AND :to IS NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') >= to_char(to_date(:from, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                        OR
                        (:from IS NULL AND :to IS NOT NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') <= to_char(to_date(:to, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                    )
                )
            ORDER BY to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') ASC;
        """, nativeQuery = true)
    Page<ExtraHours> findAllByExternalEmployeeId(@Param("employeeId") Long employeeId, @Param("period") String period, @Param("from") String from, @Param("to") String to, Pageable pageable);

    @Query(value = """
            SELECT SUM( EXTRACT (EPOCH FROM eh_extra_hours::interval) )
            FROM "clock-in-db".public2.tb_extra_hours
            WHERE eh_external_employee_id = :employeeId
            AND eh_extra_hours <> '00H00M'
            AND ( (:period IS NULL) OR eh_day_period = :period )
            AND ( (:from IS NULL AND :to IS NULL) OR
                    (
                        (:from IS NOT NULL AND :to IS NOT NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') BETWEEN to_char(to_date(:from, 'DD-MM-YYYY'), 'YYYY-MM-DD') AND to_char(to_date(:to, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                        OR
                        (:from IS NOT NULL AND :to IS NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') >= to_char(to_date(:from, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                        OR
                        (:from IS NULL AND :to IS NOT NULL AND to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') <= to_char(to_date(:to, 'DD-MM-YYYY'), 'YYYY-MM-DD'))
                    )
                )
        """, nativeQuery = true)
    Long findTotalByExternalEmployeeId(@Param("employeeId") Long employeeId, @Param("period") String period, @Param("from") String from, @Param("to") String to);

    @Query(value = """
        DELETE FROM "clock-in-db".public2.tb_extra_hours
        WHERE eh_external_employee_id = :employeeId
        AND date_trunc('day', cast(to_char(to_date(eh_day_period, 'DD-MM-YYYY'), 'YYYY-MM-DD') as DATE)) BETWEEN date_trunc('day', cast(:from as DATE)) AND date_trunc('day', cast(:to as DATE))
        """, nativeQuery = true)
    @Modifying
    void deleteByEmployeeIdPeriod(@Param("employeeId") Long employeeId, @Param("from") Date from, @Param("to") Date to);
}
