package org.com.clockinemployees.presentation.controller.employee;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.DisableEmployeeUsecase;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.EditEmployeeProfileUsecase;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.dto.EditEmployeeProfileInput;
import org.com.clockinemployees.domain.usecase.employee.getEmployeeProfileUsecase.GetEmployeeProfileUsecase;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.ListEmployeesUsecase;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesInput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.RegisterEmployeeUsecase;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.ListManagerEmployeesUsecase;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto.ListManagerEmployeesInput;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto.ListManagerEmployeesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    private final RegisterEmployeeUsecase registerEmployeeUsecase;
    private final ListEmployeesUsecase listEmployeesUsecase;
    private final DisableEmployeeUsecase disableEmployeeUsecase;
    private final EditEmployeeProfileUsecase editEmployeeProfileUsecase;
    private final GetEmployeeProfileUsecase getEmployeeProfileUsecase;
    private final ListManagerEmployeesUsecase listManagerEmployeesUsecase;

    @Override
    public ResponseEntity<RegisterEmployeeOutput> registerEmployee(
        Jwt jwt,
        RegisterEmployeeInput input
    ) {
        RegisterEmployeeOutput output = registerEmployeeUsecase.execute(jwt.getSubject(), input);
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ListEmployeesOutput> listEmployees(
        Integer page,
        Integer size,
        String name,
        String email,
        EnterprisePosition position
    ) {
        ListEmployeesOutput output = listEmployeesUsecase.execute(ListEmployeesInput.builder()
            .name(name)
            .page(page)
            .perPage(size)
            .position(position)
            .email(email)
            .build()
        );

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DisableEmployeeOutput> disableEmployee(
        Jwt jwt,
        Long employeeId
    ) {
        DisableEmployeeOutput output = disableEmployeeUsecase.execute(jwt.getSubject(), employeeId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmployeeOutput> updateProfile(
        Jwt jwt,
        EditEmployeeProfileInput input
    ) {
        EmployeeOutput output = editEmployeeProfileUsecase.execute(jwt.getSubject(), input);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ListManagerEmployeesOutput> listManaging(Jwt jwt, Integer page, Integer size, String name) {
        ListManagerEmployeesOutput output = listManagerEmployeesUsecase.execute(jwt.getSubject(), ListManagerEmployeesInput.builder()
            .page(page)
            .size(size)
            .name(name)
            .build()
        );

        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EmployeeOutput> info(
        Jwt jwt,
        Long employeeId
    ) {
        EmployeeOutput output = getEmployeeProfileUsecase.execute(jwt.getSubject(), employeeId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}
