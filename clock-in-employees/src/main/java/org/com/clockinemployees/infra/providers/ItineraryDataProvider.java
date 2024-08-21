package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Itinerary;
import org.com.clockinemployees.infra.repository.ItineraryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ItineraryDataProvider {
    private final ItineraryRepository itineraryRepository;

    public Optional<Itinerary> findByEmployee(Long employeeId) {
        return itineraryRepository.findByEmployeeId(employeeId);
    }

    public Itinerary persist(Itinerary itinerary) {
        return itineraryRepository.save(itinerary);
    }
}
