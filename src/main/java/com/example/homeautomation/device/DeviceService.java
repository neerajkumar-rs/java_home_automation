package com.example.homeautomation.device;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeviceService {

    private static final String ON = "ON";
    private static final String OFF = "OFF";

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Device setState(String name, String state) {
        Device device = deviceRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device: " + name));
        return setState(device, state, "ON".equals(state) ? 100 : 0);
    }

    public Device setState(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device id: " + id));
        if ("RGB".equals(device.getControlType())
                && (request.red() != null || request.green() != null || request.blue() != null)) {
            device.setRed(request.red() == null ? device.getRed() : request.red());
            device.setGreen(request.green() == null ? device.getGreen() : request.green());
            device.setBlue(request.blue() == null ? device.getBlue() : request.blue());
            device.setValue(Math.round((device.getRed() + device.getGreen() + device.getBlue()) / 7.65f));
        }
        return setState(device, request.state(), request.value() == null ? device.getValue() : request.value());
    }

    public Device setState(Long id, String state, Integer value) {
        return setState(id, new DeviceRequest(null, null, state, value, null, null, null, null));
    }

    public Device setState(Device device, String state, int value) {
        if (!"ON".equals(state) && !"OFF".equals(state)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "State must be ON or OFF");
        }
        if (!device.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, device.getName() + " is deactivated");
        }
        device.setState(state);
        if ("RGB".equals(device.getControlType())) {
            device.setValue(Math.round(Math.max(device.getRed(), Math.max(device.getGreen(), device.getBlue())) / 2.55f));
        } else {
            device.setValue(value);
        }
        return deviceRepository.save(device);
    }

    public Device create(String name, String controlType) {
        return create(name, controlType, null);
    }

    public Device create(String name, String controlType, String sensorType) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device name is required");
        }
        String normalizedName = name.trim();
        if (deviceRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A device with that name already exists");
        }
        String normalizedType = controlType == null || controlType.isBlank()
                ? "SWITCH" : controlType.trim().toUpperCase(Locale.ROOT);
        if (!List.of("SWITCH", "SLIDER", "RGB", "SENSOR").contains(normalizedType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Control type must be SWITCH, SLIDER, RGB, or SENSOR");
        }
        Device device = new Device(normalizedName, "OFF", normalizedType, true, 0);
        if ("SENSOR".equals(normalizedType)) {
            device.setSensorType(sensorType == null || sensorType.isBlank() ? "light" : sensorType.trim().toLowerCase(Locale.ROOT));
        }
        return deviceRepository.save(device);
    }

    public Device setActive(Long id, boolean active) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device id: " + id));
        device.setActive(active);
        return deviceRepository.save(device);
    }

    public void delete(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown device id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    public String message(Device device) {
        return device.getName() + " turned " + device.getState();
    }

    public static String normalizeState(boolean enabled) {
        return enabled ? ON : OFF;
    }
}
