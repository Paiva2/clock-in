package org.com.clockin.timeclock.infra.repository;

import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtraHoursRepository extends JpaRepository<ExtraHours, UUID> {
    Optional<ExtraHours> findByDayPeriodAndExternalEmployeeId(String dayPeriod, Long externalEmployeeId);
}
