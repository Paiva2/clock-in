package org.com.clockin.timeclock.infra.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class TimeClockListenersImpl implements TimeClockListeners {
    private final ObjectMapper objectMapper = new ObjectMapper();
}
