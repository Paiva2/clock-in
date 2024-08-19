package org.com.clockin.timeclock.domain.usecase.timeClock.updateTimeClockUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Data
public class RequestUpdateTimeClockOutput {
    private UUID timeclockId;
    private Date newRequestedTimeUpdated;
    private Date oldTimeClocked;
}