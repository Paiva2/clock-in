package org.com.clockin.timeclock.infra.dataProvider.external;

import lombok.AllArgsConstructor;
import org.com.clockin.timeclock.domain.entity.external.Employee;
import org.com.clockin.timeclock.infra.client.EmployeeClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EmployeeDataProvider {
    private final EmployeeClient employeeClient;

    public ResponseEntity<Employee> findEmployeeByResourceServerId(String externalAuthToken) {
        return employeeClient.getEmployeeInfo(externalAuthToken);
    }
}
