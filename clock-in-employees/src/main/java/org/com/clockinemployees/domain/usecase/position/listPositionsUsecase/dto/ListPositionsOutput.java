package org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.com.clockinemployees.domain.usecase.common.dto.PositionOutput;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class ListPositionsOutput {
    private Integer page;
    private Integer size;
    private Long totalItems;
    private Integer totalPages;
    private List<PositionOutput> items;
}
