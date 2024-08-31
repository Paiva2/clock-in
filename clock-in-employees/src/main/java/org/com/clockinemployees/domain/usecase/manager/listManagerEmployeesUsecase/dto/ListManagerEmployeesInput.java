package org.com.clockinemployees.domain.usecase.manager.listManagerEmployeesUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListManagerEmployeesInput {
    private Integer page;
    private Integer size;
    private String name;
}
