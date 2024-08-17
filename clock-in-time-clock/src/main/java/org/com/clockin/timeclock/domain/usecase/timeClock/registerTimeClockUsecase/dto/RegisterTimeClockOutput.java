package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegisterTimeClockOutput {
    private Long employeeId;
    private Date timeClocked;
}
