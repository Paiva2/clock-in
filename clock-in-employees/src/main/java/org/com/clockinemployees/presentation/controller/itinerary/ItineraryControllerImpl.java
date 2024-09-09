package org.com.clockinemployees.presentation.controller.itinerary;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.AddEmployeeItineraryUsecase;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryOutput;
import org.com.clockinemployees.domain.usecase.itinerary.removeEmployeeItineraryUsecase.RemoveEmployeeItineraryUsecase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ItineraryControllerImpl implements ItineraryController {
    private final AddEmployeeItineraryUsecase addEmployeeItineraryUsecase;
    private final RemoveEmployeeItineraryUsecase removeEmployeeItineraryUsecase;

    @Override
    public ResponseEntity<AddEmployeeItineraryOutput> insert(Jwt jwt, AddEmployeeItineraryInput input) {
        AddEmployeeItineraryOutput output = addEmployeeItineraryUsecase.execute(jwt.getSubject(), input);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> remove(Jwt jwt, Long employeeId) {
        removeEmployeeItineraryUsecase.execute(jwt.getSubject(), employeeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
