package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.infra.repository.EmployeeManagerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class EmployeeManagerDataProvider {
    private final EmployeeManagerRepository employeeManagerRepository;

    public Optional<EmployeeManager> findEmployeeManager(Long managerId, Long employeeId) {
        return employeeManagerRepository.findByManagerIdAndEmployeeId(managerId, employeeId);
    }
}
