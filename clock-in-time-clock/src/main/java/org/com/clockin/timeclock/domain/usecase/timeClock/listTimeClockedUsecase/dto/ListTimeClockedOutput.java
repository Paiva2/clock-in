package org.com.clockin.timeclock.domain.usecase.timeClock.listTimeClockedUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ListTimeClockedOutput {
    private String fromDate;
    private String toDate;
    private Long totalItems;
    private LinkedHashMap<String, TimeClockListDTO> items;
}
