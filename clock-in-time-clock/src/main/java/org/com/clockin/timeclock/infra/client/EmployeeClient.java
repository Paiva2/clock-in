package org.com.clockin.timeclock.infra.client;

import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "employee", url = "${gateway.url}/employee")
public interface EmployeeClient {
    @GetMapping("/info")
    ResponseEntity<Employee> getEmployeeInfo(@RequestHeader("Authorization") String token);

    @GetMapping("/info")
    ResponseEntity<Employee> getEmployeeInfo(@RequestHeader("Authorization") String token, @RequestParam("employeeId") Long employeeId);
}
