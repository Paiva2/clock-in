package org.com.clockin.timeclock.domain.strategy.dateFormatValidator;

public class DateTimeFormatStrategy {
    private final DateValidatorStrategy strategy;

    public DateTimeFormatStrategy(DateValidatorStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute(String input, String fieldName) {
        strategy.validateInputFormat(input, fieldName);
    }
}
