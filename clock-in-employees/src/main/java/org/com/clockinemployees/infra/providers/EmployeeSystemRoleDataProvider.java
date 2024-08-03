package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeeSystemRole;
import org.com.clockinemployees.infra.repository.EmployeeSystemRoleRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EmployeeSystemRoleDataProvider {
    private final EmployeeSystemRoleRepository employeeSystemRoleRepository;

    public EmployeeSystemRole create(EmployeeSystemRole employeeSystemRole) {
        return employeeSystemRoleRepository.save(employeeSystemRole);
    }
}
