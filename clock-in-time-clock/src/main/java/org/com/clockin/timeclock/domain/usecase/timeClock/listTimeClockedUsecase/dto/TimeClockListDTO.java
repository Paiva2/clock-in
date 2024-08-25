package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TimeClockListDTO {
    private LinkedList<TimeClockFilterOutput> timeClockeds;
    private String totalHoursWorkDay;
    private String totalExtraHoursDay;
}
