package org.com.clockin.timeclock.domain.strategy.dateFormatValidator;

public interface DateValidatorStrategy {
    void validateInputFormat(String inputFormat, String fieldName);
}
