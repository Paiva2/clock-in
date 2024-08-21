package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class AddEmployeeItineraryOutput {
    private String dayWorkHours;
    private String inHour;
    private String intervalInHour;
    private String intervalOutHour;
    private String outHour;
    private Long employeeId;
}
