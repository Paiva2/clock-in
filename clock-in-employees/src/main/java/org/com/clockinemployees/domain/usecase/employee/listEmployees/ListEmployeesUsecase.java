package org.com.clockinemployees.domain.usecase.employee.listEmployees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesInput;
import org.com.clockinemployees.domain.usecase.employee.listEmployees.dto.ListEmployeesOutput;
import org.com.clockinemployees.infra.providers.EmployeeDataProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Builder
public class ListEmployeesUsecase {
    private final EmployeeDataProvider employeeDataProvider;

    public ListEmployeesOutput execute(ListEmployeesInput listEmployeesInput) {
        handleDefaultPagination(listEmployeesInput);

        Page<Employee> employees = listAllEmployees(listEmployeesInput);

        return mountOutput(employees);
    }

    private void handleDefaultPagination(ListEmployeesInput listEmployeesInput) {
        if (listEmployeesInput.getPage() < 1) {
            listEmployeesInput.setPage(1);
        }
    }

    private Page<Employee> listAllEmployees(ListEmployeesInput listEmployeesInput) {
        PageRequest pageRequest = PageRequest.of(listEmployeesInput.getPage() - 1, listEmployeesInput.getPerPage(), Sort.Direction.DESC, "EM_CREATED_AT");

        return employeeDataProvider.listAllEmployees(pageRequest, listEmployeesInput.getName(), listEmployeesInput.getEmail(), listEmployeesInput.getPosition());
    }

    private ListEmployeesOutput mountOutput(Page<Employee> employees) {
        return ListEmployeesOutput.builder()
            .page(employees.getNumber() + 1)
            .size(employees.getSize())
            .totalItems(employees.getTotalElements())
            .totalPages(employees.getTotalPages())
            .employees(employees.getContent().stream().map(EmployeeOutput::toDto).toList())
            .build();
    }
}
