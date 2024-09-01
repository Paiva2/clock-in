package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.listEmployeePendingApprovals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListEmployeePendingApprovalsInput {
    private Integer page;
    private Integer size;
}
