package com.example.homeautomation.device;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceDataInitializer {

    @Bean
    CommandLineRunner seedDevices(DeviceRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Device("Light", "OFF", "SWITCH", true, 0));
                repository.save(new Device("Fan", "OFF", "SLIDER", true, 0));
                repository.save(new Device("AC", "OFF", "SLIDER", true, 0));
            } else {
                repository.findByNameIgnoreCase("Light").ifPresent(device -> {
                    device.setControlType("SWITCH");
                    repository.save(device);
                });
                repository.findByNameIgnoreCase("Fan").ifPresent(device -> {
                    device.setControlType("SLIDER");
                    repository.save(device);
                });
                repository.findByNameIgnoreCase("AC").ifPresent(device -> {
                    device.setControlType("SLIDER");
                    repository.save(device);
                });
                repository.findAll().stream()
                        .filter(device -> "KNOB".equals(device.getControlType()))
                        .forEach(device -> {
                            device.setControlType("SLIDER");
                            repository.save(device);
                        });
            }
        };
    }
}
