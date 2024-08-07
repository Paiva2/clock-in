package org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class AuthenticateEmployeeOutput {
    private Long id;
    private String email;
}
