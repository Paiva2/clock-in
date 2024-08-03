package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.infra.repository.EmployeeManagerRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EmployeeManagerDataProvider {
    private final EmployeeManagerRepository employeeManagerRepository;

    public EmployeeManager save(EmployeeManager employeeManager) {
        return employeeManagerRepository.save(employeeManager);
    }
}
