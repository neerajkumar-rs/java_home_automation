package com.example.homeautomation.device;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for creating a device. No deviceUid (server-generated UUID) and
 * no owner field (ownership comes from the SecurityContext) — the client can
 * set neither.
 */
public class DeviceCreateRequest {

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Device type is required")
    @Size(max = 50, message = "Device type must be less than 50 characters")
    private String type;

    @Size(max = 50, message = "Subtype must be less than 50 characters")
    private String subtype;

    @Size(max = 50, message = "Room must be less than 50 characters")
    private String room;

    private DeviceState state = DeviceState.OFF;

    @Min(value = 0, message = "Value must be between 0 and 100")
    @Max(value = 100, message = "Value must be between 0 and 100")
    private Integer value = 0;

    @Min(value = 0, message = "Red must be between 0 and 255")
    @Max(value = 255, message = "Red must be between 0 and 255")
    private Integer red = 255;

    @Min(value = 0, message = "Green must be between 0 and 255")
    @Max(value = 255, message = "Green must be between 0 and 255")
    private Integer green = 255;

    @Min(value = 0, message = "Blue must be between 0 and 255")
    @Max(value = 255, message = "Blue must be between 0 and 255")
    private Integer blue = 255;

    private boolean online = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public DeviceState getState() { return state; }
    public void setState(DeviceState state) { this.state = state; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public Integer getRed() { return red; }
    public void setRed(Integer red) { this.red = red; }

    public Integer getGreen() { return green; }
    public void setGreen(Integer green) { this.green = green; }

    public Integer getBlue() { return blue; }
    public void setBlue(Integer blue) { this.blue = blue; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}
