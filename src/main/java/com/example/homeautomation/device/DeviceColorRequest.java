package com.example.homeautomation.device;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Input DTO for POST /api/devices/{id}/color. */
public class DeviceColorRequest {

    @NotNull(message = "Red is required")
    @Min(value = 0, message = "Red must be between 0 and 255")
    @Max(value = 255, message = "Red must be between 0 and 255")
    private Integer red;

    @NotNull(message = "Green is required")
    @Min(value = 0, message = "Green must be between 0 and 255")
    @Max(value = 255, message = "Green must be between 0 and 255")
    private Integer green;

    @NotNull(message = "Blue is required")
    @Min(value = 0, message = "Blue must be between 0 and 255")
    @Max(value = 255, message = "Blue must be between 0 and 255")
    private Integer blue;

    @Size(max = 200, message = "Reason must be less than 200 characters")
    private String reason;

    public Integer getRed() { return red; }
    public void setRed(Integer red) { this.red = red; }

    public Integer getGreen() { return green; }
    public void setGreen(Integer green) { this.green = green; }

    public Integer getBlue() { return blue; }
    public void setBlue(Integer blue) { this.blue = blue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
