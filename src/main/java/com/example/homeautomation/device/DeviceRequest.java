package com.example.homeautomation.device;

public record DeviceRequest(String name, String controlType, String state, Integer value,
                            Integer red, Integer green, Integer blue, String sensorType) {
}