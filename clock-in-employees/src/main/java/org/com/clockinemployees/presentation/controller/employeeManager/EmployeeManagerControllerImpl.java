package org.com.clockinemployees.presentation.controller.employeeManager;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.employeeManager.InsertEmployeeManagerUsecase.InsertEmployeeManagerUsecase;
import org.com.clockinemployees.domain.usecase.employeeManager.removeEmployeeManagerUsecase.RemoveEmployeeManagerUsecase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeManagerControllerImpl implements EmployeeManagerController {
    private final InsertEmployeeManagerUsecase insertEmployeeManagerUsecase;
    private final RemoveEmployeeManagerUsecase removeEmployeeManagerUsecase;

    @Override
    public ResponseEntity<Void> addEmployeeManager(Jwt jwt, Long employeeId, Long managerId) {
        insertEmployeeManagerUsecase.execute(jwt.getSubject(), managerId, employeeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> removeEmployeeManager(Jwt jwt, Long employeeId, Long managerId) {
        removeEmployeeManagerUsecase.execute(jwt.getSubject(), managerId, employeeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
