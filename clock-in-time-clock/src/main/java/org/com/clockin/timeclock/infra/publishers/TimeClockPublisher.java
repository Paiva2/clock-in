package org.com.clockin.timeclock.infra.publishers;

import org.com.clockin.timeclock.infra.publishers.dto.PublishNewTimeClockedInput;

public interface TimeClockPublisher {
    void publishNewTimeClocked(PublishNewTimeClockedInput input);
}
