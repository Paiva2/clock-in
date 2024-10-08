package org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddEmployeeItineraryInput {
    @NotBlank
    private String hourIn;

    @NotBlank
    private String hourOut;

    @NotBlank
    private String intervalIn;

    @NotBlank
    private String intervalOut;
}
