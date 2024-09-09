package org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import org.com.clockinemployees.domain.usecase.itinerary.makeEmployeeDayWorkHours.dto.MakeEmployeeDayWorkHoursInput;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class MakeEmployeeDayWorkHoursUsecase {
    public String execute(MakeEmployeeDayWorkHoursInput input) {
        String[] hourAndMinuteIn = input.getHourIn().split(":");
        String[] hourAndMinuteOut = input.getHourOut().split(":");

        String[] hourAndMinuteIntervalIn = input.getIntervalIn().split(":");
        String[] hourAndMinuteIntervalOut = input.getIntervalOut().split(":");

        Date timeIn = dateFromHourInput(hourAndMinuteIn, 0);
        Date timeOut = dateFromHourInput(hourAndMinuteOut, 0);

        Date timeIntervalIn = dateFromHourInput(hourAndMinuteIntervalIn, 0);
        Date timeIntervalOut = dateFromHourInput(hourAndMinuteIntervalOut, 0);

        Integer inHour = Integer.parseInt(hourAndMinuteIn[0]);
        Integer outHour = Integer.parseInt(hourAndMinuteOut[0]);

        Boolean employeeClockOutNextDay = inHour > outHour;

        if (employeeClockOutNextDay) {
            timeOut = DateUtils.addDays(timeOut, 1);
        }

        Long intervalDurationDiff = (timeIntervalOut.getTime() - timeIntervalIn.getTime()) / 1000;
        Long totalWorkDayInSeconds = ((timeOut.getTime() - timeIn.getTime()) / 1000) - intervalDurationDiff;
        Long totalWorkDayHours = TimeUnit.SECONDS.toHours(totalWorkDayInSeconds) % 24;
        Long totalWorkDayMinutes = TimeUnit.SECONDS.toMinutes(totalWorkDayInSeconds) % 60;

        return buildHoursFormatString(totalWorkDayHours, totalWorkDayMinutes);
    }

    private Date dateFromHourInput(String[] hourAndMinute, Integer daysToAdd) {
        Integer hours = Integer.parseInt(hourAndMinute[0]);
        Integer minutes = Integer.parseInt(hourAndMinute[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        if (daysToAdd > 0) {
            calendar.set(Calendar.DATE, daysToAdd);
        }

        return calendar.getTime();
    }

    private String buildHoursFormatString(Long hours, Long minutes) {
        StringBuilder stringBuilder = new StringBuilder();

        if (hours < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{0}").append(":");

        if (minutes < 10) {
            stringBuilder.append("0");
        }

        stringBuilder.append("{1}");

        return MessageFormat.format(stringBuilder.toString(), hours, minutes);
    }
}
