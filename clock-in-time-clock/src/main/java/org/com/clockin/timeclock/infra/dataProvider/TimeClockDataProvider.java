package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.infra.repository.TimeClockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class TimeClockDataProvider {
    private final TimeClockRepository timeClockRepository;

    public List<TimeClock> findTimeClocksOnDayForEmployee(Long employeeExternalId, Date day) {
        return timeClockRepository.getTimeClocksOnDayByEmployee(employeeExternalId, day);
    }

    public TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockRepository.save(timeClock);
    }

    public Page<TimeClock> listAllByEmployee(Long employeeExternalId, Date startDate, Date endDate) {
        Pageable pageable = PageRequest.of(0, 90, Sort.Direction.ASC, "TC_TIME_CLOCKED");

        return timeClockRepository.findAllByEmployee(employeeExternalId, startDate, endDate, pageable);
    }

    public Optional<TimeClock> findByIdAndEmployeeId(Long employeeId, UUID timeClockId) {
        return timeClockRepository.findByExternalEmployeeIdAndId(employeeId, timeClockId);
    }

    public void deleteById(UUID timeClockId) {
        timeClockRepository.deleteById(timeClockId);
    }
}
