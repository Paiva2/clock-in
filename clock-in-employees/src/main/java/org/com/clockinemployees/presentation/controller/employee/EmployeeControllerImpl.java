package org.com.clockinemployees.presentation.controller.employee;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.RegisterEmployeeUsecase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    private final RegisterEmployeeUsecase registerEmployeeUsecase;

    @Override
    public ResponseEntity registerEmployee() {
        return null;
    }
}
