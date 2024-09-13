package org.com.clockin.timeclock.domain.usecase.extraHours.cleanEmployeeExtraHours.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CleanEmployeeExtraHoursInput {
    @Min(value = 1)
    @Max(value = 12)
    @NotBlank
    private Integer month;

    @Min(value = 1999)
    @NotBlank
    private Integer year;
}
