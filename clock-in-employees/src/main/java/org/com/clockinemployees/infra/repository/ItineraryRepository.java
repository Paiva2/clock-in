package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Optional<Itinerary> findByEmployeeId(Long employeeId);
}
