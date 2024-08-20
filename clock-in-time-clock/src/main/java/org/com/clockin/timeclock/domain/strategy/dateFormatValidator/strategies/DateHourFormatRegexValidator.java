package org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies;

import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateValidatorStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception.InvalidDataInputFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateHourFormatRegexValidator implements DateValidatorStrategy {
    //dd-mm-yyyy HH:mm:ss
    private final static String DATE_FORMAT_REGEX = "^(0[1-9]|1\\d|2\\d|3[01])-(0[1-9]|1\\d|2\\d|3[01])-(19|20)\\d{2}\\s+(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|[1-5][0-9]):(0[0-9]|[1-5][0-9])$";

    @Override
    public void validateInputFormat(String dateInput, String fieldName) {
        Pattern pattern = Pattern.compile(DATE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(dateInput);

        if (!matcher.matches()) {
            throw new InvalidDataInputFormatException(fieldName);
        }
    }
}
