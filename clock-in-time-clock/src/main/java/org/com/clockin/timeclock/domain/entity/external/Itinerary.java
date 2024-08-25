package org.com.clockin.timeclock.domain.entity.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Itinerary {
    private String dayWorkHours;
    private String inHour;
    private String intervalInHour;
    private String intervalOutHour;
    private String outHour;
}
