package org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MakeEmployeeDayWorkHoursInput {
    private String hourIn;
    private String hourOut;
    private String intervalIn;
    private String intervalOut;
}
