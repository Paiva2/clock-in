package org.com.clockinemployees.presentation.controller.employee;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.DisableEmployeeUsecase;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.ListEmployeesUsecase;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesInput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.RegisterEmployeeUsecase;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    private final RegisterEmployeeUsecase registerEmployeeUsecase;
    private final ListEmployeesUsecase listEmployeesUsecase;
    private final DisableEmployeeUsecase disableEmployeeUsecase;

    @Override
    public ResponseEntity<RegisterEmployeeOutput> registerEmployee(
        @RequestBody @Valid RegisterEmployeeInput input
    ) {
        RegisterEmployeeOutput output = registerEmployeeUsecase.execute(input);
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ListEmployeesOutput> listEmployees(
        @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
        @RequestParam(name = "name", required = false) String name,
        @RequestParam(name = "email", required = false) String email,
        @RequestParam(name = "position", required = false) EnterprisePosition position
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
        @PathVariable("employeeId") Long employeeId
    ) {
        Long mockSuperiorId = 1L; // todo: fix
        DisableEmployeeOutput output = disableEmployeeUsecase.execute(mockSuperiorId, employeeId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}
