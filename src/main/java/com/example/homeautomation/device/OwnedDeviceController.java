package com.example.homeautomation.device;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Device API (JWT required). All endpoints are owner-scoped: a device that
 * does not exist or belongs to someone else returns 404. Controllers only
 * accept/return DTOs, never entities.
 */
@RestController
@RequestMapping("/api/devices")
public class OwnedDeviceController {

    private final OwnedDeviceService deviceService;

    public OwnedDeviceController(OwnedDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> list() {
        return ResponseEntity.ok(deviceService.listMine());
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getMine(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.updateMine(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceService.deleteMine(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/state")
    public ResponseEntity<DeviceResponse> setState(@PathVariable Long id,
                                                   @Valid @RequestBody DeviceStateRequest request) {
        return ResponseEntity.ok(deviceService.setState(id, request));
    }

    @PostMapping("/{id}/color")
    public ResponseEntity<DeviceResponse> setColor(@PathVariable Long id,
                                                   @Valid @RequestBody DeviceColorRequest request) {
        return ResponseEntity.ok(deviceService.setColor(id, request));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<DeviceLogResponse>> logs(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getLogs(id));
    }
}
