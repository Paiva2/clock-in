package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.infra.repository.EmployeePositionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Component
public class EmployeePositionDataProvider {
    private final EmployeePositionRepository employeePositionRepository;

    public EmployeePosition create(EmployeePosition employeePosition) {
        return employeePositionRepository.save(employeePosition);
    }

    public void remove(EmployeePosition employeePosition) {
        employeePositionRepository.delete(employeePosition);
    }

    public Optional<EmployeePosition> findHrByEmployeeId(Long superiorId) {
        return employeePositionRepository.findHrByEmployeeId(superiorId);
    }

    public Optional<EmployeePosition> findByEmployeeId(Long superiorId) {
        return employeePositionRepository.findByEmployeeId(superiorId);
    }

    public Set<EmployeePosition> findAllByEmployeeId(Long employeeId) {
        return employeePositionRepository.findAllByEmployeeId(employeeId);
    }
}
