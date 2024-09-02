package org.com.clockin.timeclock.domain.usecase.timeClock.filterEmployeeTimeClockUsecase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FilterEmployeeTimeClockInput {
    @Max(11)
    @Min(0)
    @NotNull
    private Integer month;

    @Min(2024)
    @NotNull
    private Integer year;
}
