package org.com.clockin.timeclock.infra.dataProvider;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.infra.repository.TimeClockRepository;
import org.springframework.stereotype.Component;

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
}
