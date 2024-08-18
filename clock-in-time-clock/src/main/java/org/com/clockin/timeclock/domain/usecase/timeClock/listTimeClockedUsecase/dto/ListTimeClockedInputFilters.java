package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListTimeClockedInputFilters {
    private String startDate;
    private String endDate;
}
