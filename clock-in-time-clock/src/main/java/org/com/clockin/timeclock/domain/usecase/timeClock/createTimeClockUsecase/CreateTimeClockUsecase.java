package org.com.clockin.timeclock.domain.usecase.timeClock.createTimeClockUsecase;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class CreateTimeClockUsecase {
    private final TimeClockDataProvider timeClockDataProvider;

    public void execute(Long externalEmployeeId) {
        TimeClock timeClock = fillTimeClock(externalEmployeeId);
        persistTimeClock(timeClock);
    }

    private TimeClock fillTimeClock(Long employeeId) {
        return TimeClock.builder()
            .timeClocked(new Date())
            .externalEmployeeId(employeeId)
            .build();
    }

    private TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockDataProvider.persistTimeClock(timeClock);
    }
}
