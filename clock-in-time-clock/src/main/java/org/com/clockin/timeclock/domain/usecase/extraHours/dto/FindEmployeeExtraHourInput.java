package org.com.clockin.timeclock.domain.usecase.extraHours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindEmployeeExtraHourInput {
    private Integer page;
    private Integer perPage;
    private String period;
}
