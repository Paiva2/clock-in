package org.com.clockin.timeclock.infra.publishers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockin.timeclock.domain.entity.TimeClock;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PublishNewTimeClockedInput {
    private Long employeeId;
    private Date timeClocked;
    private TimeClock.Event event;
}
