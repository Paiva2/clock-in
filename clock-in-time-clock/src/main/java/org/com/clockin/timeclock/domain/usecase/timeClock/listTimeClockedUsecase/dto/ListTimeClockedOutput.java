package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;
import org.com.clockin.timeclock.domain.utils.DateHandler;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ListTimeClockedOutput {
    private String fromDate;
    private String toDate;
    private Long totalItems;
    private List<TimeClockFilterOutput> items;

    @NoArgsConstructor
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
        private List<UpdateApprovals> pendingApprovals;

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
                .pendingApprovals(timeClock.getPendingUpdateApprovals().stream().map(UpdateApprovals::toDto).toList())
                .build();
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        @Builder
        public static class UpdateApprovals {
            private UUID id;
            private Date timeClockUpdated;
            private Boolean approved;
            private String reason;
            private Date createdAt;
            private Date updatedAt;

            public static UpdateApprovals toDto(PendingUpdateApproval pendingUpdateApproval) {
                return UpdateApprovals.builder()
                    .id(pendingUpdateApproval.getId())
                    .approved(pendingUpdateApproval.getApproved())
                    .reason(pendingUpdateApproval.getReason())
                    .createdAt(pendingUpdateApproval.getCreatedAt())
                    .updatedAt(pendingUpdateApproval.getUpdatedAt())
                    .timeClockUpdated(pendingUpdateApproval.getTimeClockUpdated())
                    .build();
            }
        }
    }
}
