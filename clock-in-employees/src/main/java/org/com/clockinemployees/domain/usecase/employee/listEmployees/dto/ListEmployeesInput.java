package org.com.clockinemployees.domain.usecase.employee.listEmployees.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.enums.EnterprisePosition;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListEmployeesInput {
    private EnterprisePosition position;
    private Integer page;
    private Integer perPage;
    private String name;
    private String email;
}
