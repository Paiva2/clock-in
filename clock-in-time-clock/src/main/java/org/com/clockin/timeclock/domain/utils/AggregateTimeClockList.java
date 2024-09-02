package org.com.clockin.timeclock.domain.utils;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.ExtraHours;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.TimeClockFilterOutput;
import org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto.TimeClockListDTO;
import org.com.clockin.timeclock.infra.dataProvider.ExtraHoursDataProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

import static org.com.clockin.timeclock.domain.entity.TimeClock.Event.*;
import static org.com.clockin.timeclock.domain.entity.TimeClock.Event.INTERVAL_OUT;

@Component
@AllArgsConstructor
public class AggregateTimeClockList {
    private final ExtraHoursDataProvider extraHoursDataProvider;
    private final DateHandler dateHandler;

    public LinkedHashMap<String, TimeClockListDTO> execute(List<TimeClock> timeClocks, Employee employee) {
        LinkedHashMap<String, TimeClockListDTO> timeClockeds = new LinkedHashMap<>();

        for (TimeClock timeClock : timeClocks) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeClock.getTimeClocked());

            String day = DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked());
            String month = DateHandler.extractMonthFromDate(timeClock.getTimeClocked());
            String year = DateHandler.extractYearFromDate(timeClock.getTimeClocked());

            String key = day + "-" + month + "-" + year;

            TimeClockListDTO timeClockList = timeClockeds.get(key);

            if (Objects.isNull(timeClockList)) {
                Optional<ExtraHours> findPeriodExtraHours = extraHoursDataProvider.findByDayPeriod(key, employee.getId());

                TimeClockListDTO timeClockListDtos = TimeClockListDTO.builder()
                    .timeClockeds(
                        new LinkedList<>() {{
                            add(TimeClockFilterOutput.toDto(timeClock));
                        }}
                    )
                    .totalExtraHoursDay(findPeriodExtraHours.isPresent() ? findPeriodExtraHours.get().getExtraHours() : "0")
                    .totalHoursWorkDay(null)
                    .build();

                timeClockeds.put(key, timeClockListDtos);
            } else {
                timeClockList.getTimeClockeds().add(TimeClockFilterOutput.toDto(timeClock));
            }
        }

        calculateAndSetWorkHoursOfDay(timeClockeds);

        return timeClockeds;
    }

    private void calculateAndSetWorkHoursOfDay(LinkedHashMap<String, TimeClockListDTO> timeClockeds) {
        timeClockeds.keySet().forEach(key -> {
            TimeClockListDTO timeClockListDto = timeClockeds.get(key);

            if (timeClockListDto.getTimeClockeds().isEmpty()) return;

            Optional<TimeClockFilterOutput> timeClockedIn = findTimeClockedByEventOnList(timeClockListDto, IN);
            if (timeClockedIn.isEmpty()) return;

            Optional<TimeClockFilterOutput> timeClockedOut = findTimeClockedByEventOnList(timeClockListDto, OUT);

            Long hoursWorkedOnSeconds = 0L;

            Optional<TimeClockFilterOutput> timeClockedIntervalIn = findTimeClockedByEventOnList(timeClockListDto, INTERVAL_IN);

            if (timeClockedIntervalIn.isPresent()) {
                Duration timeWorkedFromInToIntervalIn = Duration.between(timeClockedIn.get().getTimeClocked().toInstant(), timeClockedIntervalIn.get().getTimeClocked().toInstant());

                hoursWorkedOnSeconds += timeWorkedFromInToIntervalIn.toSeconds();

                Optional<TimeClockFilterOutput> timeClockedIntervalOut = findTimeClockedByEventOnList(timeClockListDto, INTERVAL_OUT);

                if (timeClockedIntervalOut.isPresent() && timeClockedOut.isPresent()) {
                    Duration hoursWorkedFromIntervalOutToOutInSeconds = Duration.between(timeClockedIntervalOut.get().getTimeClocked().toInstant(), timeClockedOut.get().getTimeClocked().toInstant());
                    hoursWorkedOnSeconds += hoursWorkedFromIntervalOutToOutInSeconds.toSeconds();
                }
            } else if (timeClockedOut.isPresent()) {
                Duration hoursWorkedFromInToOutInSeconds = Duration.between(timeClockedIn.get().getTimeClocked().toInstant(), timeClockedOut.get().getTimeClocked().toInstant());
                hoursWorkedOnSeconds += hoursWorkedFromInToOutInSeconds.toSeconds();
            }

            Long hoursWorked = hoursWorkedOnSeconds / 3600;
            Long minutes = (hoursWorkedOnSeconds % 3600) / 60;

            timeClockListDto.setTotalHoursWorkDay(dateHandler.buildHoursFormatString(hoursWorked, minutes));
        });
    }

    private Optional<TimeClockFilterOutput> findTimeClockedByEventOnList(TimeClockListDTO timeClockListDto, TimeClock.Event event) {
        return timeClockListDto.getTimeClockeds().stream().filter(tc -> tc.getEventType().equals(event)).findAny();
    }
}
