package com.example.homeautomation.device;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByNameIgnoreCase(String name);
}
