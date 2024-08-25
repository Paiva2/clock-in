package org.com.clockin.timeclock.domain.utils;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class DateHandler {
    public Date parseDate(String date, String pattern) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(pattern);

        return formatter.parse(date);
    }

    public String formatDate(Date date, String pattern) {
        DateFormat formatter = new SimpleDateFormat(pattern);

        return formatter.format(date);
    }

    public ZonedDateTime getTodayOnMaxHour() {
        return ZonedDateTime.now().with(LocalTime.MAX);
    }

    public Date getTodayMinusDays(Integer days) {
        ZonedDateTime today = getTodayOnMaxHour();

        return Date.from(today.minusDays(days).toInstant());
    }

    public static String extractDayNumberFromDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getDayOfMonth() < 10 ? insertLeadingZero(localDate.getDayOfMonth()) : java.lang.String.valueOf(localDate.getDayOfMonth());
    }

    public static String extractYearFromDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return java.lang.String.valueOf(localDate.getYear());
    }

    public static String extractMonthFromDate(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.getMonthValue() < 10 ? insertLeadingZero(localDate.getMonthValue()) : java.lang.String.valueOf(localDate.getMonthValue());
    }

    private static String insertLeadingZero(Integer value) {
        return java.lang.String.format("%02d", value);
    }

    public String buildHoursFormatString(Long hours, Long minutes) {
        StringBuilder stringBuilder = new StringBuilder();

        if (hours < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{0}").append("H");

        if (minutes < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{1}");

        return MessageFormat.format(stringBuilder.toString(), hours, minutes) + "M";
    }
}
