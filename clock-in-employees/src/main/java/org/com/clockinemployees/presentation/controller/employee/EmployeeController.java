package org.com.clockinemployees.presentation.controller.employee;

import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("employee")
public interface EmployeeController {
    @PostMapping("/register")
    ResponseEntity<RegisterEmployeeOutput> registerEmployee(@AuthenticationPrincipal Jwt jwt, RegisterEmployeeInput input);

    @GetMapping("/list")
    ResponseEntity<ListEmployeesOutput> listEmployees(Integer page, Integer size, String name, String email, EnterprisePosition position);

    @GetMapping("/userinfo")
    ResponseEntity<String> userinfo(Jwt jwt);

    @PutMapping("/disable/{employeeId}")
    ResponseEntity<DisableEmployeeOutput> disableEmployee(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId);
}
