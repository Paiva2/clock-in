package org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.enums.EnterprisePosition;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EditEmployeePositionOutput {
    private Long userId;
    private EnterprisePosition newPosition;
    private Long superiorId;
}
