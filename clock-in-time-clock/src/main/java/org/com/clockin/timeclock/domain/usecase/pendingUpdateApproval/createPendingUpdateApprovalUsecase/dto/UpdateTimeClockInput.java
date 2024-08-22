package org.com.clockin.timeclock.domain.usecase.pendingUpdateApproval.createPendingUpdateApprovalUsecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateTimeClockInput {
    @NotBlank
    private String updatedTimeClocked;

    @Size(max = 200)
    private String reason;
}