package org.com.clockinemployees.presentation.controller.position;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.EditEmployeePositionUsecase;
import org.com.clockinemployees.domain.usecase.position.editEmployeePositionUsecase.dto.EditEmployeePositionOutput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.ListPositionsUsecase;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsInput;
import org.com.clockinemployees.domain.usecase.position.listPositionsUsecase.dto.ListPositionsOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PositionControllerimpl implements PositionController {
    private final EditEmployeePositionUsecase editEmployeePositionUsecase;
    private final ListPositionsUsecase listPositionsUsecase;

    @Override
    public ResponseEntity<ListPositionsOutput> listPositions(
        Jwt jwt,
        @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "5") Integer size
    ) {
        ListPositionsOutput output = listPositionsUsecase.execute(jwt.getSubject(), ListPositionsInput.builder()
            .page(page)
            .size(size)
            .build()
        );
        return new ResponseEntity<>(output, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EditEmployeePositionOutput> editEmployeePosition(
        Jwt jwt,
        @PathVariable("employeeId") Long employeeId,
        @PathVariable("positionId") Long positionId
    ) {
        EditEmployeePositionOutput output = editEmployeePositionUsecase.execute(jwt.getSubject(), employeeId, positionId);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}
