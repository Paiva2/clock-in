package org.com.clockin.timeclock.domain.usecase.extraHours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockin.timeclock.domain.entity.ExtraHours;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FilterEmployeeExtraHourOutput {
    private Integer page;
    private Integer perPage;
    private Long totalItems;
    private Integer totalPages;
    private String totalExtra;
    private String dayPeriod;
    private List<ExtraHours> extraHours;
}
