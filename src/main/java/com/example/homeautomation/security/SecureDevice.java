package com.example.homeautomation.security;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secure_devices",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"owner_email", "name"}, name = "uniq_owner_device_name")
       })
public class SecureDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Device UID is required")
    @Size(max = 64, message = "Device UID must be less than 64 characters")
    @Column(name = "device_uid", unique = true, nullable = false)
    private String deviceUid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_email", referencedColumnName = "email", nullable = false)
    @JsonBackReference
    private SecureUser owner;
    
    @Transient
    private String ownerEmail; // Transient field for easy access
    
    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must be less than 100 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Device type is required")
    @Size(max = 50, message = "Device type must be less than 50 characters")
    @Column(nullable = false)
    private String type;
    
    @Size(max = 50, message = "Subtype must be less than 50 characters")
    private String subtype;
    
    @Size(max = 50, message = "Room must be less than 50 characters")
    private String room;
    
    @NotBlank(message = "State is required")
    @Size(max = 3, message = "State must be less than 4 characters")
    @Column(nullable = false)
    private String state = "OFF";
    
    @NotNull(message = "Value is required")
    @Column(nullable = false)
    private Integer value = 0;
    
    @NotNull(message = "Red value is required")
    @Column(nullable = false)
    private Integer red = 255;
    
    @NotNull(message = "Green value is required")
    @Column(nullable = false)
    private Integer green = 255;
    
    @NotNull(message = "Blue value is required")
    @Column(nullable = false)
    private Integer blue = 255;
    
    @Column(name = "online")
    private boolean online = false;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "last_communication")
    private LocalDateTime lastCommunication;
    
    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String capabilities = "{}";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // One-to-many: Device can be shared with multiple users
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<DeviceShare> shares = new HashSet<>();
    
    // Automation rules for this device
    @OneToMany(mappedBy = "device")
    private Set<SecureAutomationRule> automationRules = new HashSet<>();
    
    // State change logs
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private Set<DeviceStateLog> stateLogs = new HashSet<>();
    
    // Constructors
    public SecureDevice() {
        this.createdAt = LocalDateTime.now();
        this.lastCommunication = LocalDateTime.now();
    }
    
    public SecureDevice(String deviceUid, SecureUser owner, String name, String type) {
        this();
        this.deviceUid = deviceUid;
        this.owner = owner;
        this.name = name;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceUid() {
        return deviceUid;
    }
    
    public void setDeviceUid(String deviceUid) {
        this.deviceUid = deviceUid;
    }
    
    public SecureUser getOwner() {
        return owner;
    }
    
    public void setOwner(SecureUser owner) {
        this.owner = owner;
        this.ownerEmail = owner != null ? owner.getEmail() : null;
    }
    
    public String getOwnerEmail() {
        if (owner != null) {
            return owner.getEmail();
        }
        return ownerEmail;
    }
    
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSubtype() {
        return subtype;
    }
    
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public void setValue(Integer value) {
        this.value = value;
    }
    
    public Integer getRed() {
        return red;
    }
    
    public void setRed(Integer red) {
        this.red = red;
    }
    
    public Integer getGreen() {
        return green;
    }
    
    public void setGreen(Integer green) {
        this.green = green;
    }
    
    public Integer getBlue() {
        return blue;
    }
    
    public void setBlue(Integer blue) {
        this.blue = blue;
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
        if (online) {
            this.lastCommunication = LocalDateTime.now();
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getLastCommunication() {
        return lastCommunication;
    }
    
    public void setLastCommunication(LocalDateTime lastCommunication) {
        this.lastCommunication = lastCommunication;
    }
    
    public String getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<DeviceShare> getShares() {
        return shares;
    }
    
    public void setShares(Set<DeviceShare> shares) {
        this.shares = shares;
    }
    
    public void addShare(DeviceShare share) {
        share.setDevice(this);
        this.shares.add(share);
    }
    
    public Set<SecureAutomationRule> getAutomationRules() {
        return automationRules;
    }
    
    public void setAutomationRules(Set<SecureAutomationRule> automationRules) {
        this.automationRules = automationRules;
    }
    
    public Set<DeviceStateLog> getStateLogs() {
        return stateLogs;
    }
    
    public void setStateLogs(Set<DeviceStateLog> stateLogs) {
        this.stateLogs = stateLogs;
    }
    
    public void addStateLog(DeviceStateLog log) {
        log.setDevice(this);
        this.stateLogs.add(log);
    }
    
    // Business methods
    public boolean isSharedWith(String userEmail) {
        return shares.stream()
                .anyMatch(share -> share.getSharedWith().getEmail().equals(userEmail) 
                        && share.isActive());
    }
    
    public boolean canUserView(String userEmail) {
        if (getOwnerEmail().equals(userEmail)) {
            return true;
        }
        return shares.stream()
                .anyMatch(share -> share.getSharedWith().getEmail().equals(userEmail) 
                        && share.isCanView() 
                        && share.isActive());
    }
    
    public boolean canUserControl(String userEmail) {
        if (getOwnerEmail().equals(userEmail)) {
            return true;
        }
        return shares.stream()
                .anyMatch(share -> share.getSharedWith().getEmail().equals(userEmail) 
                        && share.isCanControl() 
                        && share.isActive());
    }
    
    public void updateState(String newState, Integer newValue, String source, String changedBy) {
        // Log the previous state
        DeviceStateLog log = new DeviceStateLog();
        log.setPreviousState(this.state);
        log.setNewState(newState);
        log.setPreviousValue(this.value != null ? this.value.toString() : null);
        log.setNewValue(newValue != null ? newValue.toString() : null);
        log.setChangeReason(source);
        log.setTriggeredBy(changedBy);
        
        // Update current state
        this.state = newState;
        this.value = newValue;
        
        // Add to logs
        this.addStateLog(log);
    }
    
    public void updateColor(Integer red, Integer green, Integer blue, String source, String changedBy) {
        DeviceStateLog log = new DeviceStateLog();
        log.setChangeReason(source);
        log.setTriggeredBy(changedBy);
        
        this.red = red;
        this.green = green;
        this.blue = blue;
        
        this.addStateLog(log);
    }
    
    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.online) {
            this.lastCommunication = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "SecureDevice{" +
                "id=" + id +
                ", deviceUid='" + deviceUid + '\'' +
                ", ownerEmail='" + getOwnerEmail() + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", active=" + active +
                '}';
    }
}