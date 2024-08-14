package org.com.clockinemployees.domain.usecase.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PositionOutput {
    private Long id;
    private EnterprisePosition name;

    public static PositionOutput toDto(Position position) {
        return PositionOutput.builder()
            .id(position.getId())
            .name(position.getName())
            .build();
    }
}
