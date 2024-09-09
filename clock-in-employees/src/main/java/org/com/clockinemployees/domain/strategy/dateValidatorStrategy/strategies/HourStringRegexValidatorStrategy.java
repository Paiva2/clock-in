package org.com.clockinemployees.domain.strategy.dateValidatorStrategy.strategies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.com.clockinemployees.domain.strategy.dateValidatorStrategy.HourValidatorStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Builder
@Component
public class HourStringRegexValidatorStrategy implements HourValidatorStrategy {
    private final static String HOUR_PATTERN = "^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$"; // HH:MM

    @Override
    public boolean isValid(String date) {
        Pattern pattern = Pattern.compile(HOUR_PATTERN);
        Matcher matcher = pattern.matcher(date);

        return matcher.matches();
    }
}
