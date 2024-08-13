package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.infra.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class PositionDataProvider {
    private final PositionRepository positionRepository;

    public Optional<Position> findPositionById(Long id) {
        return positionRepository.findById(id);
    }

    public Page<Position> findAll(Pageable pageable) {
        return positionRepository.findAll(pageable);
    }
}
