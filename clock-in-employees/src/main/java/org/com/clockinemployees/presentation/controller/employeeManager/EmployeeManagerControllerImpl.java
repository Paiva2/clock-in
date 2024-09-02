package org.com.clockinemployees.presentation.controller.employeeManager;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.employee.InsertEmployeeManagerUsecase.InsertEmployeeManagerUsecase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeManagerControllerImpl implements EmployeeManagerController {
    private final InsertEmployeeManagerUsecase insertEmployeeManagerUsecase;

    @Override
    public ResponseEntity<Void> addEmployeeManager(Jwt jwt, Long employeeId, Long managerId) {
        insertEmployeeManagerUsecase.execute(jwt.getSubject(), managerId, employeeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
