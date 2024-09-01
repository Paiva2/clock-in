package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto;

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

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListEmployeePendingApprovalOutput {
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private List<EmployeePendingApprovalOutput> pendingApprovals;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class EmployeePendingApprovalOutput {
        private UUID id;
        private String timeClockUpdated;
        private Boolean approved;
        private String reason;
        private Date createdAt;
        private TimeClockOutput timeClock;

        public static EmployeePendingApprovalOutput toDto(PendingUpdateApproval pendingUpdateApproval) {
            return EmployeePendingApprovalOutput.builder()
                .id(pendingUpdateApproval.getId())
                .approved(pendingUpdateApproval.getApproved())
                .timeClock(TimeClockOutput.toDto(pendingUpdateApproval.getTimeClock()))
                .timeClockUpdated(DateHandler.formatDateToOutput(pendingUpdateApproval.getTimeClockUpdated()))
                .reason(pendingUpdateApproval.getReason())
                .createdAt(pendingUpdateApproval.getCreatedAt())
                .build();
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class TimeClockOutput {
            private UUID id;
            private Long externalEmployeeId;
            private String timeClockedFormatted;
            private TimeClock.Event eventType;
            private Date updatedAt;
            private String day;
            private String month;
            private String year;

            public static TimeClockOutput toDto(TimeClock timeClock) {
                return TimeClockOutput.builder()
                    .id(timeClock.getId())
                    .externalEmployeeId(timeClock.getExternalEmployeeId())
                    .timeClockedFormatted(DateHandler.formatDateToOutput(timeClock.getTimeClocked()))
                    .eventType(timeClock.getEvent())
                    .updatedAt(timeClock.getUpdatedAt())
                    .day(DateHandler.extractDayNumberFromDate(timeClock.getTimeClocked()))
                    .month(DateHandler.extractMonthFromDate(timeClock.getTimeClocked()))
                    .year(DateHandler.extractYearFromDate(timeClock.getTimeClocked()))
                    .build();
            }
        }
    }
}
