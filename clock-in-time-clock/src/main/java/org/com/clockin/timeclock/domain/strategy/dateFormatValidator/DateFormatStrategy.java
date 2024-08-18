package org.com.clockin.timeclock.domain.strategy.dateFormatValidator;

public class DateFormatStrategy {
    private final DateValidatorStrategy strategy;

    public DateFormatStrategy(DateValidatorStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute(String input, String fieldName) {
        strategy.validateInputFormat(input, fieldName);
    }
}
