package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.infra.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Component
public class EmployeeDataProvider {
    private final EmployeeRepository employeeRepository;

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Optional<Employee> findByFullName(String firstName, String lastName) {
        return employeeRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    public Page<Employee> listAllEmployees(PageRequest pageRequest, String employeeName, String email, EnterprisePosition enterprisePosition) {
        String positionName = Objects.nonNull(enterprisePosition) ? enterprisePosition.name() : null;

        return employeeRepository.findAllPaginated(employeeName, email, positionName, pageRequest);
    }
}
