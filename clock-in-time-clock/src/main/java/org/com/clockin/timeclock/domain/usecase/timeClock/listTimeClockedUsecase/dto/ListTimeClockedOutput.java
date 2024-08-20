package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.utils.DateHandler;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Builder
@Data
public class ListTimeClockedOutput {
    private String fromDate;
    private String toDate;
    private Long totalItems;
    private List<TimeClockFilterOutput> items;

    @AllArgsConstructor
    @Data
    @Builder
    public static class TimeClockFilterOutput {
        private UUID id;
        private Long externalEmployeeId;
        private Date timeClocked;
        private TimeClock.Event eventType;
        private Date updatedAt;
        private String day;
        private String month;
        private String year;

        public static TimeClockFilterOutput toDto(TimeClock timeClock) {
            return TimeClockFilterOutput.builder()
                .id(timeClock.getId())
                .externalEmployeeId(timeClock.getExternalEmployeeId())
                .timeClocked(timeClock.getTimeClocked())
                .eventType(timeClock.getEvent())
                .updatedAt(timeClock.getUpdatedAt())
                .day(DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked()))
                .month(DateHandler.extractMonthFromDate(timeClock.getTimeClocked()))
                .year(DateHandler.extractYearFromDate(timeClock.getTimeClocked()))
                .build();
        }
    }
}
