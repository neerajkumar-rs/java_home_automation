package com.example.homeautomation.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Secure Device Controller
 * 
 * Security Implementation:
 * 1. All endpoints require authentication
 * 2. All data access automatically filtered by user context
 * 3. Strict permission checking for each operation
 * 4. Proper error handling for security violations
 */
@RestController
@RequestMapping("/api/v1/secure/devices")
@CrossOrigin(origins = "*")
public class SecureDeviceController {
    
    private final SecureDeviceService deviceService;
    
    public SecureDeviceController(SecureDeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    /**
     * Get all devices accessible to current user
     * Includes owned devices + shared devices
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SecureDevice>> getAllDevices() {
        try {
            List<SecureDevice> devices = deviceService.getAllUserDevices();
            return ResponseEntity.ok(devices);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get devices owned by current user
     */
    @GetMapping("/owned")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SecureDevice>> getOwnedDevices() {
        try {
            List<SecureDevice> devices = deviceService.getOwnedDevices();
            return ResponseEntity.ok(devices);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get devices shared with current user
     */
    @GetMapping("/shared")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SecureDevice>> getSharedDevices() {
        try {
            List<SecureDevice> devices = deviceService.getSharedDevices();
            return ResponseEntity.ok(devices);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get specific device by ID (with permission check)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDevice> getDeviceById(@PathVariable Long id) {
        try {
            SecureDevice device = deviceService.getDeviceById(id);
            return ResponseEntity.ok(device);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create new device (automatically sets current user as owner)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDevice> createDevice(@RequestBody DeviceCreateRequest request) {
        try {
            SecureDevice deviceDTO = new SecureDevice();
            deviceDTO.setDeviceUid(request.getDeviceUid());
            deviceDTO.setName(request.getName());
            deviceDTO.setType(request.getType());
            deviceDTO.setSubtype(request.getSubtype());
            deviceDTO.setRoom(request.getRoom());
            deviceDTO.setState(request.getState());
            deviceDTO.setValue(request.getValue());
            deviceDTO.setOnline(request.isOnline());
            
            SecureDevice createdDevice = deviceService.createDevice(deviceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Update existing device (requires ownership or control permission)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDevice> updateDevice(@PathVariable Long id, 
                                                      @RequestBody DeviceUpdateRequest request) {
        try {
            SecureDevice deviceDTO = new SecureDevice();
            deviceDTO.setName(request.getName());
            deviceDTO.setType(request.getType());
            deviceDTO.setSubtype(request.getSubtype());
            deviceDTO.setRoom(request.getRoom());
            deviceDTO.setCapabilities(request.getCapabilities());
            deviceDTO.setState(request.getState());
            deviceDTO.setValue(request.getValue());
            deviceDTO.setRed(request.getRed());
            deviceDTO.setGreen(request.getGreen());
            deviceDTO.setBlue(request.getBlue());
            deviceDTO.setOnline(request.isOnline());
            
            SecureDevice updatedDevice = deviceService.updateDevice(id, deviceDTO);
            return ResponseEntity.ok(updatedDevice);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Delete device (requires ownership)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Control device (turn on/off, set value)
     */
    @PostMapping("/{id}/control")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDevice> controlDevice(@PathVariable Long id,
                                                     @RequestBody DeviceControlRequest request) {
        try {
            SecureDevice device = deviceService.controlDevice(id, request.getState(), request.getValue());
            return ResponseEntity.ok(device);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Set device color
     */
    @PostMapping("/{id}/color")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDevice> setDeviceColor(@PathVariable Long id,
                                                       @RequestBody DeviceColorRequest request) {
        try {
            SecureDevice device = deviceService.setDeviceColor(id, 
                    request.getRed(), request.getGreen(), request.getBlue());
            return ResponseEntity.ok(device);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Search devices accessible to current user
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SecureDevice>> searchDevices(@RequestParam String query) {
        try {
            List<SecureDevice> devices = deviceService.searchDevices(query);
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get device statistics for dashboard
     */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getDeviceStatistics() {
        try {
            SecureDeviceService.DeviceStatistics stats = deviceService.getDeviceStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("totalDevices", stats.getTotalOwned() + stats.getTotalShared());
            response.put("ownedDevices", stats.getTotalOwned());
            response.put("sharedDevices", stats.getTotalShared());
            response.put("onlineDevices", stats.getOnlineOwned() + stats.getOnlineShared());
            response.put("devicesByType", stats.getDevicesByType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check permission for specific device
     */
    @GetMapping("/{id}/permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SecureDeviceService.DevicePermission> checkPermission(@PathVariable Long id) {
        try {
            SecureDeviceService.DevicePermission permission = deviceService.checkPermission(id);
            return ResponseEntity.ok(permission);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint (public)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Secure Device API");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    // ============================================================================
    // REQUEST DTO CLASSES
    // ============================================================================
    
    public static class DeviceCreateRequest {
        private String deviceUid;
        private String name;
        private String type;
        private String subtype;
        private String room;
        private String state = "OFF";
        private Integer value = 0;
        private boolean online = false;
        
        // Getters and setters
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
        
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
    
    public static class DeviceUpdateRequest {
        private String name;
        private String type;
        private String subtype;
        private String room;
        private String capabilities = "{}";
        private String state;
        private Integer value;
        private Integer red;
        private Integer green;
        private Integer blue;
        private boolean online;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getSubtype() { return subtype; }
        public void setSubtype(String subtype) { this.subtype = subtype; }
        
        public String getRoom() { return room; }
        public void setRoom(String room) { this.room = room; }
        
        public String getCapabilities() { return capabilities; }
        public void setCapabilities(String capabilities) { this.capabilities = capabilities; }
        
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
    }
    
    public static class DeviceControlRequest {
        private String state = "ON";
        private Integer value = 100;
        
        // Getters and setters
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }
    
    public static class DeviceColorRequest {
        private Integer red = 255;
        private Integer green = 255;
        private Integer blue = 255;
        
        // Getters and setters
        public Integer getRed() { return red; }
        public void setRed(Integer red) { this.red = red; }
        
        public Integer getGreen() { return green; }
        public void setGreen(Integer green) { this.green = green; }
        
        public Integer getBlue() { return blue; }
        public void setBlue(Integer blue) { this.blue = blue; }
    }
}