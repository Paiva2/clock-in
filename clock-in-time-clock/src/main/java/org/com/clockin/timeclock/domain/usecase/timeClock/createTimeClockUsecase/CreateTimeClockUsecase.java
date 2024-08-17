package org.com.clockin.timeclock.domain.usecase.timeClock.createTimeClockUsecase;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreateTimeClockUsecase {
    private final TimeClockDataProvider timeClockDataProvider;

    public void execute(PublishNewTimeClockedInput input) {
        TimeClock timeClock = fillTimeClock(input);
        persistTimeClock(timeClock);
    }

    private TimeClock fillTimeClock(PublishNewTimeClockedInput input) {
        return TimeClock.builder()
            .timeClocked(input.getTimeClocked())
            .externalEmployeeId(input.getEmployeeId())
            .build();
    }

    private TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockDataProvider.persistTimeClock(timeClock);
    }
}
