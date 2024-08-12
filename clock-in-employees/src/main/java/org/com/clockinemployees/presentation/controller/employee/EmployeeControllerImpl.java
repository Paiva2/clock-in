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
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.EditEmployeePositionUsecase;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.dto.EditEmployeePositionOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class EmployeeControllerImpl implements EmployeeController {
    private final static String CLAIM_SUBJECT_ID = "user_application_id";

    private final RegisterEmployeeUsecase registerEmployeeUsecase;
    private final ListEmployeesUsecase listEmployeesUsecase;
    private final DisableEmployeeUsecase disableEmployeeUsecase;
    private final EditEmployeePositionUsecase editEmployeePositionUsecase;

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
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable("employeeId") Long employeeId
    ) {
        String subjectId = jwt.getClaimAsString(CLAIM_SUBJECT_ID);

        DisableEmployeeOutput output = disableEmployeeUsecase.execute(Long.valueOf(subjectId), employeeId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EditEmployeePositionOutput> editEmployeePosition(
        Jwt jwt,
        @PathVariable("employeeId") Long employeeId,
        @PathVariable("positionId") Long positionId
    ) {
        EditEmployeePositionOutput output = editEmployeePositionUsecase.execute(jwt.getSubject(), employeeId, positionId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<String> userinfo(
        @AuthenticationPrincipal Jwt jwt
    ) {
        String subjectId = jwt.getClaimAsString(CLAIM_SUBJECT_ID);
        return new ResponseEntity<>("Sub id: " + jwt.getSubject(), HttpStatus.OK);
    }
}
