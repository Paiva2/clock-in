package org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.dto.AuthenticateEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.dto.AuthenticateEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.authenticateEmployeeUsecase.exception.EmployeeInvalidCredentials;
import org.com.clockinemployees.domain.usecase.employee.common.exception.EmployeeNotFoundException;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@AllArgsConstructor
@Builder
@Service
public class AuthenticateEmployeeUsecase {
    private final EmployeeDataProvider employeeDataProvider;

    private PasswordEncoder passwordEncoder;

    public AuthenticateEmployeeOutput execute(AuthenticateEmployeeInput input) {
        Employee employee = findEmployee(input.getEmail());

        if (Objects.nonNull(employee.getDisabledAt())) {
            throw new EmployeeNotFoundException();
        }

        comparePasswords(input.getPassword(), employee.getPassword());

        return mountOutput(employee);
    }

    private Employee findEmployee(String email) {
        return employeeDataProvider.findByEmail(email).orElseThrow(EmployeeNotFoundException::new);
    }

    private void comparePasswords(String rawPassword, String hashPassword) {
        Boolean passwordMatches = passwordEncoder.matches(rawPassword, hashPassword);

        if (!passwordMatches) {
            throw new EmployeeInvalidCredentials();
        }
    }

    private AuthenticateEmployeeOutput mountOutput(Employee employee) {
        return AuthenticateEmployeeOutput.builder()
            .id(employee.getId())
            .email(employee.getEmail())
            .build();
    }
}
