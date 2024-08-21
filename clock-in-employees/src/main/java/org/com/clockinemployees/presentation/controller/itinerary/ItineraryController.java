package org.com.clockinemployees.presentation.controller.itinerary;

import jakarta.validation.Valid;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("employee")
public interface ItineraryController {
    @PostMapping("/itinerary")
    ResponseEntity<AddEmployeeItineraryOutput> insert(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid AddEmployeeItineraryInput input);
}
