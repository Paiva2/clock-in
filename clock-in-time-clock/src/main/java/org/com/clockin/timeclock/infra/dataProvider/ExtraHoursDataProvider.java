package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.com.clockin.timeclock.infra.repository.ExtraHoursRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ExtraHoursDataProvider {
    private final ExtraHoursRepository extraHoursRepository;

    public Optional<ExtraHours> findByDayPeriod(String dayPeriod, Long employeeId) {
        return extraHoursRepository.findByDayPeriodAndExternalEmployeeId(dayPeriod, employeeId);
    }

    public Page<ExtraHours> findAllByEmployee(Long employeeId, String period, String from, String to, Pageable pageable) {
        return extraHoursRepository.findAllByExternalEmployeeId(employeeId, period, from, to, pageable);
    }

    public Long findTotalByEmployee(Long employeeId, String period, String from, String to) {
        return extraHoursRepository.findTotalByExternalEmployeeId(employeeId, period, from, to);
    }

    public ExtraHours persist(ExtraHours extraHours) {
        return extraHoursRepository.save(extraHours);
    }

    public void removeById(UUID id) {
        extraHoursRepository.deleteById(id);
    }

    public void removeAllByEmployeeId(Long employeeId, Date from, Date to) {
        extraHoursRepository.deleteByEmployeeIdPeriod(employeeId, from, to);
    }
}
