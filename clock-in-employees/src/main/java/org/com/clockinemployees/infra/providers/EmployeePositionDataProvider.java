package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.infra.repository.EmployeePositionRepository;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class EmployeePositionDataProvider {
    private final EmployeePositionRepository employeePositionRepository;

    public EmployeePosition create(EmployeePosition employeePosition) {
        return employeePositionRepository.save(employeePosition);
    }
}
