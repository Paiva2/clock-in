package org.com.clockin.timeclock.domain.usecase.timeClock.filterTimeClockUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockin.timeclock.domain.entity.PendingUpdateApproval;
import org.com.clockin.timeclock.domain.entity.TimeClock;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FilterTimeClockOutput {
    private UUID id;
    private Long externalEmployeeId;
    private Date timeClocked;
    private TimeClock.Event eventType;
    private Date updatedAt;
    private String day;
    private String month;
    private String year;
    private List<UpdateApprovals> pendingApprovals;

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
