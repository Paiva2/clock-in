package org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class DisableEmployeeOutput {
    private Long employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private Long actionDoneBySuperiorId;
}
