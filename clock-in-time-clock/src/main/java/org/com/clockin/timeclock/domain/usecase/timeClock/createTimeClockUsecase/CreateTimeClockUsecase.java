package org.com.clockin.timeclock.domain.usecase.timeClock.createTimeClockUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.exception.MaxTimeClockedExceptionForDay;
import org.com.clockin.timeclock.infra.dataProvider.TimeClockDataProvider;
import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Builder
public class CreateTimeClockUsecase {
    private final TimeClockDataProvider timeClockDataProvider;

    public void execute(PublishNewTimeClockedInput input) {
        checkTimeClocksToday(input.getEmployeeId());

        TimeClock timeClock = fillTimeClock(input);

        persistTimeClock(timeClock);
    }

    private TimeClock fillTimeClock(PublishNewTimeClockedInput input) {
        return TimeClock.builder()
            .timeClocked(input.getTimeClocked())
            .externalEmployeeId(input.getEmployeeId())
            .build();
    }

    private void checkTimeClocksToday(Long externalEmployeeId) {
        Integer quantityClockedToday = timeClockedQuantityToday(externalEmployeeId);

        if (quantityClockedToday >= 4) {
            throw new MaxTimeClockedExceptionForDay();
        }
    }

    private Integer timeClockedQuantityToday(Long employeeId) {
        return timeClockDataProvider.findTimeClocksCountTodayForEmployee(employeeId);
    }

    private TimeClock persistTimeClock(TimeClock timeClock) {
        return timeClockDataProvider.persistTimeClock(timeClock);
    }
}
