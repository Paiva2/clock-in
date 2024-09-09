package org.com.clockinemployees.presentation.controller.itinerary;

import jakarta.validation.Valid;
import org.com.clockinemployees.domain.usecase.common.dto.ItineraryOutput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryInput;
import org.com.clockinemployees.domain.usecase.itinerary.addEmployeeItineraryUsecase.dto.AddEmployeeItineraryOutput;
import org.com.clockinemployees.domain.usecase.itinerary.editEmployeeItinerary.dto.EditEmployeeItineraryInput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RequestMapping("employee")
public interface ItineraryController {
    @PostMapping("/{employeeId}/itinerary")
    ResponseEntity<AddEmployeeItineraryOutput> insert(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @RequestBody @Valid AddEmployeeItineraryInput input);

    @DeleteMapping("/{employeeId}/itinerary")
    ResponseEntity<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId);

    @PutMapping("/{employeeId}/itinerary")
    ResponseEntity<ItineraryOutput> edit(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @RequestBody @Valid EditEmployeeItineraryInput input);
}
