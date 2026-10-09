package com.example.homeautomation.device;

/**
 * Output DTO for a device. Controllers return this, never the entity,
 * so the API surface is decoupled from the persistence model and lazy
 * associations (owner, shares, logs) are never serialized.
 */
public class DeviceResponse {

    private Long id;
    private String deviceUid;
    private String name;
    private String type;
    private String subtype;
    private String room;
    private String state;
    private Integer value;
    private Integer red;
    private Integer green;
    private Integer blue;
    private boolean online;
    private boolean active;
    private String ownerEmail;

    public DeviceResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceUid() { return deviceUid; }
    public void setDeviceUid(String deviceUid) { this.deviceUid = deviceUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
