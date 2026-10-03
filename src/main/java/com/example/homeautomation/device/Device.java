package com.example.homeautomation.device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 3)
    private String state;

    @Column(nullable = false, length = 12, columnDefinition = "varchar(12) default 'SWITCH'")
    private String controlType;

    @Column(length = 20)
    private String sensorType;

    @Column(nullable = false, columnDefinition = "boolean default 1")
    private boolean active;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int value;

    @Column(nullable = false, columnDefinition = "integer default 255")
    private int red;

    @Column(nullable = false, columnDefinition = "integer default 255")
    private int green;

    @Column(nullable = false, columnDefinition = "integer default 255")
    private int blue;

    protected Device() {
    }

    public Device(String name, String state) {
        this(name, state, "SWITCH", true, "ON".equals(state) ? 100 : 0);
    }

    public Device(String name, String state, String controlType, boolean active, int value) {
        this.name = name;
        this.state = state;
        this.controlType = controlType;
        this.active = active;
        this.value = value;
        this.red = 255;
        this.green = 255;
        this.blue = 255;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = Math.max(0, Math.min(100, value));
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = clampColor(red);
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = clampColor(green);
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = clampColor(blue);
    }

    private int clampColor(int color) {
        return Math.max(0, Math.min(255, color));
    }
}
