package org.com.clockinemployees.domain.usecase.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.clockinemployees.domain.entity.Itinerary;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ItineraryOutput {
    private String dayWorkHours;
    private String inHour;
    private String intervalInHour;
    private String intervalOutHour;
    private String outHour;

    public static ItineraryOutput toDto(Itinerary itinerary) {
        return ItineraryOutput.builder()
            .dayWorkHours(itinerary.getDayWorkHours())
            .inHour(itinerary.getInHour())
            .outHour(itinerary.getOutHour())
            .intervalInHour(itinerary.getIntervalInHour())
            .intervalOutHour(itinerary.getIntervalOutHour())
            .build();
    }
}
