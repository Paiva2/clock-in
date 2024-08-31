package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.infra.repository.EmployeeManagerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class EmployeeManagerDataProvider {
    private final EmployeeManagerRepository employeeManagerRepository;

    public Optional<EmployeeManager> findEmployeeManager(Long managerId, Long employeeId) {
        return employeeManagerRepository.findByManagerIdAndEmployeeId(managerId, employeeId);
    }

    public List<EmployeeManager> findEmployeeManagers(Long employeeId) {
        return employeeManagerRepository.findAllByEmployeeId(employeeId);
    }

    public Page<EmployeeManager> findManagerEmployees(Long managerId, String employeeName, Pageable pageable) {
        return employeeManagerRepository.findAllByManagerId(managerId, employeeName, pageable);
    }
}
