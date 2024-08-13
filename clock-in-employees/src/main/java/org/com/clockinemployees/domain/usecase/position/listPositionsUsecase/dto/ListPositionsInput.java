package org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ListPositionsInput {
    private Integer page;
    private Integer size;
}
