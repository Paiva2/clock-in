package org.com.clockinemployees.domain.usecase.employee.enableEmployeeUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class EnableEmployeeOutput {
    private Long employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private Long actionDoneBySuperiorId;
}
