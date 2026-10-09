package com.example.homeautomation.device;

import java.time.LocalDateTime;

/** Output DTO for a device state-change log entry. */
public record DeviceLogResponse(
        Long id,
        String previousState,
        String newState,
        String previousValue,
        String newValue,
        String changeReason,
        String triggeredBy,
        LocalDateTime logTime) {
}
