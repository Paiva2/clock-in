package org.com.clockinemployees.presentation.controller.position;

import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.dto.EditEmployeePositionOutput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("employee")
public interface PositionController {
    @GetMapping("/positions/list")
    ResponseEntity<ListPositionsOutput> listPositions(@AuthenticationPrincipal Jwt jwt, @RequestParam("page") Integer page, @RequestParam("size") Integer size);

    @PatchMapping("/{employeeId}/edit/position/{positionId}")
    ResponseEntity<EditEmployeePositionOutput> editEmployeePosition(@AuthenticationPrincipal Jwt jwt, @PathVariable("employeeId") Long employeeId, @PathVariable("positionId") Long positionId);
}
