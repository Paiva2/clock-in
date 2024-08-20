package org.com.clockin.timeclock.domain.usecase.timeClock.registerTimeClockUsecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RegisterTimeClockInput {
    @NotBlank
    private String timeClocked;

    @NotNull
    private TimeClock.Event event;
}
