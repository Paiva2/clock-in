package org.com.clockinemployees.domain.strategy.dateValidatorStrategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Builder
@Component
public class HourValidator {
    private final HourValidatorStrategy hourValidatorStrategy;

    public boolean validate(String hour) {
        return hourValidatorStrategy.isValid(hour);
    }
}
