package org.com.clockinemployees.presentation.controller.employee;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.RegisterEmployeeUsecase;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    private final RegisterEmployeeUsecase registerEmployeeUsecase;

    @Override
    public ResponseEntity<RegisterEmployeeOutput> registerEmployee(
        @RequestBody @Valid RegisterEmployeeInput input
    ) {
        RegisterEmployeeOutput output = registerEmployeeUsecase.execute(input);
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }
}
