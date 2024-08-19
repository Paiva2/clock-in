package org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies;

import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateValidatorStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception.InvalidHourTimeFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFormatRegexValidator implements DateValidatorStrategy {
    private final static String HOUR_PATTERN = "(?:[01]\\d|2[0-3]):(?:[0-5]\\d):(?:[0-5]\\d)"; // HH:MM:SS

    @Override
    public void validateInputFormat(String hourInput, String fieldName) {
        Pattern pattern = Pattern.compile(HOUR_PATTERN);
        Matcher matcher = pattern.matcher(hourInput);

        if (!matcher.matches()) {
            throw new InvalidHourTimeFormatException();
        }
    }
}
