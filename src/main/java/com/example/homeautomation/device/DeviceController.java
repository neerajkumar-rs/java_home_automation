package com.example.homeautomation.device;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("s")
    public List<Device> getDevices() {
        return deviceService.findAll();
    }

    @PostMapping
    public Device addDevice(@RequestBody DeviceRequest request) {
        return deviceService.create(request.name(), request.controlType(), request.sensorType());
    }

    @PostMapping("/{id}/state")
    public Map<String, String> setDeviceState(@PathVariable Long id, @RequestBody DeviceRequest request) {
        Device device = deviceService.setState(id, request);
        return response(device);
    }

    @PostMapping("/{id}/activate")
    public Device activate(@PathVariable Long id) {
        return deviceService.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public Device deactivate(@PathVariable Long id) {
        return deviceService.setActive(id, false);
    }

    @DeleteMapping("/{id}")
    public void removeDevice(@PathVariable Long id) {
        deviceService.delete(id);
    }

    @PostMapping("/light/on")
    public Map<String, String> turnLightOn() {
        return update("Light", true);
    }

    @PostMapping("/light/off")
    public Map<String, String> turnLightOff() {
        return update("Light", false);
    }

    @PostMapping("/fan/on")
    public Map<String, String> turnFanOn() {
        return update("Fan", true);
    }

    @PostMapping("/fan/off")
    public Map<String, String> turnFanOff() {
        return update("Fan", false);
    }

    @PostMapping("/ac/on")
    public Map<String, String> turnAcOn() {
        return update("AC", true);
    }

    @PostMapping("/ac/off")
    public Map<String, String> turnAcOff() {
        return update("AC", false);
    }

    private Map<String, String> update(String deviceName, boolean enabled) {
        Device device = deviceService.setState(deviceName, DeviceService.normalizeState(enabled));
        return response(device);
    }

    private Map<String, String> response(Device device) {
        return Map.of("message", deviceService.message(device), "name", device.getName(), "state", device.getState());
    }
}
