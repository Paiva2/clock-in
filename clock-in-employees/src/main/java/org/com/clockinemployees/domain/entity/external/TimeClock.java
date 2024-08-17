package org.com.clockinemployees.domain.entity.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TimeClock {
    private UUID id;
    private Date timeClocked;
    private Long externalEmployeeId;
    private Date createdAt;
    private Date updatedAt;
}
