package com.example.homeautomation.device;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices")
public class DevicesController {

    private final DeviceService deviceService;

    public DevicesController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> getDevices() {
        return deviceService.findAll();
    }
}
