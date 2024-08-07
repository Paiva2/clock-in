package org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticateEmployeeInput {
    @Email
    private String email;

    @Size(min = 6)
    private String password;
}
