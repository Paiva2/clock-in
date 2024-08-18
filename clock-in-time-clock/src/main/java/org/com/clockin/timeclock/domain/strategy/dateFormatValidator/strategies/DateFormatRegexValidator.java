package org.com.clockin.timeclock.domain.strategy.dateFormatValidator.strategies;

import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.DateValidatorStrategy;
import org.com.clockin.timeclock.domain.strategy.dateFormatValidator.exception.InvalidDataInputFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatRegexValidator implements DateValidatorStrategy {
    //dd-mm-yyyy
    private final static String DATE_FORMAT_REGEX = "^(0?[1-9]|[12][0-9]|3[01])[\\/\\-](0?[1-9]|1[012])[\\/\\-]\\d{4}$";

    @Override
    public void validateInputFormat(String dateInput, String fieldName) {
        Pattern pattern = Pattern.compile(DATE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(dateInput);

        if (!matcher.matches()) {
            throw new InvalidDataInputFormatException(fieldName);
        }
    }
}
