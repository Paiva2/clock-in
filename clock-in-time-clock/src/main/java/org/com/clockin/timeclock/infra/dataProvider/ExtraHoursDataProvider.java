package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.com.clockin.timeclock.infra.repository.ExtraHoursRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ExtraHoursDataProvider {
    private final ExtraHoursRepository extraHoursRepository;

    public Optional<ExtraHours> findByDayPeriod(String dayPeriod, Long employeeId) {
        return extraHoursRepository.findByDayPeriodAndExternalEmployeeId(dayPeriod, employeeId);
    }

    public ExtraHours persist(ExtraHours extraHours) {
        return extraHoursRepository.save(extraHours);
    }
}
