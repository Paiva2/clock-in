package org.com.clockinemployees.presentation.controller.employee;

import jakarta.validation.Valid;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.disableEmployeeUsecase.dto.DisableEmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.editEmployeeProfileUsecase.dto.EditEmployeeProfileInput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeOutput;
import org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto.ListManagerEmployeesOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequestMapping("employee")
public interface EmployeeController {
    @PostMapping("/register")
    ResponseEntity<RegisterEmployeeOutput> registerEmployee(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid RegisterEmployeeInput input);

    @GetMapping("/list")
    ResponseEntity<ListEmployeesOutput> listEmployees(@RequestParam(name = "page", required = false, defaultValue = "1") Integer page, @RequestParam(name = "size", required = false, defaultValue = "20") Integer size, @RequestParam(name = "name", required = false) String name, @RequestParam(name = "email", required = false) String email, @RequestParam(name = "position", required = false) EnterprisePosition position);

    @GetMapping("/info")
    ResponseEntity<EmployeeOutput> info(@AuthenticationPrincipal Jwt jwt, @RequestParam(value = "employeeId", required = false) Long employeeId);

    @PutMapping("/disable/{employeeId}")
    ResponseEntity<DisableEmployeeOutput> disableEmployee(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId);

    @PutMapping("/update/profile")
    ResponseEntity<EmployeeOutput> updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid EditEmployeeProfileInput input);

    @GetMapping("/managing/list")
    ResponseEntity<ListManagerEmployeesOutput> listManaging(@AuthenticationPrincipal Jwt jwt, @RequestParam(value = "page", required = false, defaultValue = "1") Integer page, @RequestParam(value = "size", required = false, defaultValue = "20") Integer size, @RequestParam(value = "name", required = false) String name);
}
