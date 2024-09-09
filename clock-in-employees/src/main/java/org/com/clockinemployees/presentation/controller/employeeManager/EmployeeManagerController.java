package org.com.clockinemployees.presentation.controller.employeeManager;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("employee")
public interface EmployeeManagerController {
    @PostMapping("/{employeeId}/manager/{managerId}")
    ResponseEntity<Void> addEmployeeManager(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @PathVariable("managerId") Long managerId);

    @DeleteMapping("/{employeeId}/manager/{managerId}")
    ResponseEntity<Void> removeEmployeeManager(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @PathVariable("managerId") Long managerId);
}
