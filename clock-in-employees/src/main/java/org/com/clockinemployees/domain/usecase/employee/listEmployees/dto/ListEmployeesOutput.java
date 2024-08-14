package org.com.clockinemployees.domain.usecase.employee.listEmployees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.usecase.common.dto.EmployeeOutput;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListEmployeesOutput {
    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;
    private List<EmployeeOutput> employees;
}
