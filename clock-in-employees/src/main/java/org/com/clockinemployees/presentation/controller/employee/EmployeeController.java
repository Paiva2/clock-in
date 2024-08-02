package org.com.clockinemployees.presentation.controller.employee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
public interface EmployeeController {
    @PostMapping("/register")
    ResponseEntity registerEmployee();
}
