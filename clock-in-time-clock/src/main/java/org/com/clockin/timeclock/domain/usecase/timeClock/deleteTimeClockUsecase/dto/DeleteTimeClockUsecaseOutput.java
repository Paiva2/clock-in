package org.com.clockin.timeclock.domain.usecase.timeClock.deleteTimeClockUsecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DeleteTimeClockUsecaseOutput {
    private UUID id;
}
