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

@AllArgsConstructor
@Component
public class TimeClockDataProvider {
    private final TimeClockRepository timeClockRepository;

    public Integer findTimeClocksCountTodayForEmployee(Long employeeExternalId) {
        return timeClockRepository.countTimeClocksTodayByEmployee(employeeExternalId);
    }

    public TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockRepository.save(timeClock);
    }

    public Page<TimeClock> listAllByEmployee(Long employeeExternalId, Date startDate, Date endDate) {
        Pageable pageable = PageRequest.of(0, 90, Sort.Direction.ASC, "TC_TIME_CLOCKED");

        return timeClockRepository.findAllByEmployee(employeeExternalId, startDate, endDate, pageable);
    }
}
