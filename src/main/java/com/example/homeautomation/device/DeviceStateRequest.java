package com.example.homeautomation.device;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Input DTO for POST /api/devices/{id}/state. */
public class DeviceStateRequest {

    @NotNull(message = "State is required (ON or OFF)")
    private DeviceState state;

    @Min(value = 0, message = "Value must be between 0 and 100")
    @Max(value = 100, message = "Value must be between 0 and 100")
    private Integer value;

    @Size(max = 200, message = "Reason must be less than 200 characters")
    private String reason;

    public DeviceState getState() { return state; }
    public void setState(DeviceState state) { this.state = state; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
