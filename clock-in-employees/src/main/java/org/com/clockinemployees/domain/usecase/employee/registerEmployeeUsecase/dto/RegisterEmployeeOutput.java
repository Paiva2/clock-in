package org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Builder
@Data
public class RegisterEmployeeOutput {
    private Long employeeId;
    private String employeeEmail;
    private Date createdAt;
}
