package com.example.homeautomation.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Secure Device Service with STRICT data isolation
 * 
 * Key Security Principles:
 * 1. Every query automatically filters by current user's email
 * 2. No user can access another user's devices
 * 3. All operations validate ownership/permissions
 */
@Service
@Transactional
public class SecureDeviceService {
    
    private final SecureDeviceRepository deviceRepository;
    private final SecureUserRepository userRepository;
    
    public SecureDeviceService(SecureDeviceRepository deviceRepository, SecureUserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get current authenticated user's email from security context
     * This is the foundation of data isolation
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Returns email (configured in UserDetailsService)
        }
        throw new SecurityException("User not authenticated");
    }
    
    // ============================================================================
    // DEVICE MANAGEMENT WITH STRICT ISOLATION
    // ============================================================================
    
    /**
     * Get all devices for current user (owner + shared)
     * Automatically filters by user context
     */
    @PreAuthorize("isAuthenticated()")
    public List<SecureDevice> getAllUserDevices() {
        String userEmail = getCurrentUserEmail();
        return deviceRepository.findAllAccessibleDevices(userEmail);
    }
    
    /**
     * Get only devices owned by current user
     */
    @PreAuthorize("isAuthenticated()")
    public List<SecureDevice> getOwnedDevices() {
        String userEmail = getCurrentUserEmail();
        return deviceRepository.findAllByOwnerEmail(userEmail);
    }
    
    /**
     * Get devices shared with current user
     */
    @PreAuthorize("isAuthenticated()")
    public List<SecureDevice> getSharedDevices() {
        String userEmail = getCurrentUserEmail();
        return deviceRepository.findSharedWithUser(userEmail);
    }
    
    /**
     * Get device by ID with strict permission check
     * Throws exception if user doesn't have access
     */
    @PreAuthorize("isAuthenticated()")
    public SecureDevice getDeviceById(Long deviceId) {
        String userEmail = getCurrentUserEmail();
        
        // Check if user has access to this device
        if (!deviceRepository.hasAccess(deviceId, userEmail)) {
            throw new SecurityException("Access denied to device " + deviceId);
        }
        
        // This query already validates ownership in repository
        Optional<SecureDevice> device = deviceRepository.findByIdAndOwnerEmail(deviceId, userEmail);
        
        // If not found as owner, try as shared device
        if (device.isEmpty()) {
            return deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new RuntimeException("Device not found"));
        }
        
        return device.get();
    }
    
    /**
     * Create new device - automatically sets current user as owner
     */
    @PreAuthorize("isAuthenticated()")
    public SecureDevice createDevice(SecureDevice deviceDTO) {
        String userEmail = getCurrentUserEmail();
        
        // Get current user
        SecureUser owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create device
        SecureDevice device = new SecureDevice();
        device.setDeviceUid(deviceDTO.getDeviceUid());
        device.setOwner(owner);
        device.setName(deviceDTO.getName());
        device.setType(deviceDTO.getType());
        device.setSubtype(deviceDTO.getSubtype());
        device.setRoom(deviceDTO.getRoom());
        device.setState(deviceDTO.getState());
        device.setValue(deviceDTO.getValue());
        device.setOnline(deviceDTO.isOnline());
        
        // Save device (automatically sets owner relationship)
        return deviceRepository.save(device);
    }
    
    /**
     * Update device - only if user owns it or has control permission
     */
    @PreAuthorize("isAuthenticated()")
    public SecureDevice updateDevice(Long deviceId, SecureDevice deviceDTO) {
        String userEmail = getCurrentUserEmail();
        
        // Check if user can control this device
        if (!deviceRepository.canControl(deviceId, userEmail)) {
            throw new SecurityException("Permission denied to update device");
        }
        
        SecureDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        
        // Update only permitted fields (not ownership!)
        device.setName(deviceDTO.getName());
        device.setType(deviceDTO.getType());
        device.setSubtype(deviceDTO.getSubtype());
        device.setRoom(deviceDTO.getRoom());
        device.setCapabilities(deviceDTO.getCapabilities());
        
        // Log the change
        String changeBy = userEmail;
        String changeSource = "USER";
        
        // Update state if changed
        if (!device.getState().equals(deviceDTO.getState()) || 
            !device.getValue().equals(deviceDTO.getValue())) {
            device.updateState(deviceDTO.getState(), deviceDTO.getValue(), 
                              changeSource, changeBy);
        }
        
        // Update color if changed
        if (!device.getRed().equals(deviceDTO.getRed()) || 
            !device.getGreen().equals(deviceDTO.getGreen()) || 
            !device.getBlue().equals(deviceDTO.getBlue())) {
            device.updateColor(deviceDTO.getRed(), deviceDTO.getGreen(), deviceDTO.getBlue(),
                             changeSource, changeBy);
        }
        
        device.setOnline(deviceDTO.isOnline());
        
        return deviceRepository.save(device);
    }
    
    /**
     * Delete device - only if user owns it
     */
    @PreAuthorize("isAuthenticated()")
    public void deleteDevice(Long deviceId) {
        String userEmail = getCurrentUserEmail();
        
        // Check ownership (only owners can delete)
        Optional<SecureDevice> device = deviceRepository.findByIdAndOwnerEmail(deviceId, userEmail);
        if (device.isEmpty()) {
            throw new SecurityException("Only device owner can delete device");
        }
        
        deviceRepository.deleteById(deviceId);
    }
    
    /**
     * Control device (turn on/off) - requires control permission
     */
    @PreAuthorize("isAuthenticated()")
    public SecureDevice controlDevice(Long deviceId, String state, Integer value) {
        String userEmail = getCurrentUserEmail();
        
        // Check if user can control this device
        if (!deviceRepository.canControl(deviceId, userEmail)) {
            throw new SecurityException("Permission denied to control device");
        }
        
        SecureDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        
        device.updateState(state, value, "USER", userEmail);
        device.setLastCommunication(LocalDateTime.now());
        
        return deviceRepository.save(device);
    }
    
    /**
     * Set device color - requires control permission
     */
    @PreAuthorize("isAuthenticated()")
    public SecureDevice setDeviceColor(Long deviceId, Integer red, Integer green, Integer blue) {
        String userEmail = getCurrentUserEmail();
        
        // Check if user can control this device
        if (!deviceRepository.canControl(deviceId, userEmail)) {
            throw new SecurityException("Permission denied to control device");
        }
        
        SecureDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        
        device.updateColor(red, green, blue, "USER", userEmail);
        
        return deviceRepository.save(device);
    }
    
    /**
     * Search devices accessible to current user
     */
    @PreAuthorize("isAuthenticated()")
    public List<SecureDevice> searchDevices(String searchTerm) {
        String userEmail = getCurrentUserEmail();
        return deviceRepository.searchAccessibleDevices(userEmail, searchTerm);
    }
    
    /**
     * Get device statistics for current user
     */
    @PreAuthorize("isAuthenticated()")
    public DeviceStatistics getDeviceStatistics() {
        String userEmail = getCurrentUserEmail();
        
        DeviceStatistics stats = new DeviceStatistics();
        
        // Count owned devices
        List<SecureDevice> ownedDevices = deviceRepository.findAllByOwnerEmail(userEmail);
        stats.setTotalOwned(ownedDevices.size());
        stats.setOnlineOwned((int) ownedDevices.stream().filter(SecureDevice::isOnline).count());
        stats.setActiveOwned((int) ownedDevices.stream().filter(SecureDevice::isActive).count());
        
        // Count shared devices
        List<SecureDevice> sharedDevices = deviceRepository.findSharedWithUser(userEmail);
        stats.setTotalShared(sharedDevices.size());
        stats.setOnlineShared((int) sharedDevices.stream().filter(SecureDevice::isOnline).count());
        
        // Count by type
        List<Object[]> typeCounts = deviceRepository.countByTypeAndOwnerEmail(userEmail);
        typeCounts.forEach(obj -> {
            stats.getDevicesByType().put((String) obj[0], ((Long) obj[1]).intValue());
        });
        
        return stats;
    }
    
    /**
     * Check user permission for specific device
     */
    @PreAuthorize("isAuthenticated()")
    public DevicePermission checkPermission(Long deviceId) {
        String userEmail = getCurrentUserEmail();
        
        DevicePermission permission = new DevicePermission();
        permission.setDeviceId(deviceId);
        permission.setUserEmail(userEmail);
        permission.setHasAccess(deviceRepository.hasAccess(deviceId, userEmail));
        permission.setCanControl(deviceRepository.canControl(deviceId, userEmail));
        
        // Additional checks can be added here
        
        return permission;
    }
    
    // ============================================================================
    // SUPPORTING CLASSES
    // ============================================================================
    
    public static class DeviceStatistics {
        private int totalOwned;
        private int onlineOwned;
        private int activeOwned;
        private int totalShared;
        private int onlineShared;
        private java.util.Map<String, Integer> devicesByType = new java.util.HashMap<>();
        
        // Getters and setters
        public int getTotalOwned() { return totalOwned; }
        public void setTotalOwned(int totalOwned) { this.totalOwned = totalOwned; }
        
        public int getOnlineOwned() { return onlineOwned; }
        public void setOnlineOwned(int onlineOwned) { this.onlineOwned = onlineOwned; }
        
        public int getActiveOwned() { return activeOwned; }
        public void setActiveOwned(int activeOwned) { this.activeOwned = activeOwned; }
        
        public int getTotalShared() { return totalShared; }
        public void setTotalShared(int totalShared) { this.totalShared = totalShared; }
        
        public int getOnlineShared() { return onlineShared; }
        public void setOnlineShared(int onlineShared) { this.onlineShared = onlineShared; }
        
        public java.util.Map<String, Integer> getDevicesByType() { return devicesByType; }
        public void setDevicesByType(java.util.Map<String, Integer> devicesByType) { 
            this.devicesByType = devicesByType; 
        }
    }
    
    public static class DevicePermission {
        private Long deviceId;
        private String userEmail;
        private boolean hasAccess;
        private boolean canControl;
        
        // Getters and setters
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public boolean isHasAccess() { return hasAccess; }
        public void setHasAccess(boolean hasAccess) { this.hasAccess = hasAccess; }
        
        public boolean isCanControl() { return canControl; }
        public void setCanControl(boolean canControl) { this.canControl = canControl; }
    }
}