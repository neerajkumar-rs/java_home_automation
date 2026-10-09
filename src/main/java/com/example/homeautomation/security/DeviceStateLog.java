package com.example.homeautomation.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Device State Log entity for tracking device state changes
 */
@Entity
@Table(name = "device_state_logs")
public class DeviceStateLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private SecureDevice device;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_email", referencedColumnName = "email", nullable = false)
    private SecureUser owner;
    
    @Column(name = "previous_state", length = 50)
    private String previousState;
    
    @Column(name = "new_state", length = 50, nullable = false)
    private String newState;
    
    @Column(name = "state_value")
    private String stateValue;
    
    @Column(name = "previous_value")
    private String previousValue;
    
    @Column(name = "new_value")
    private String newValue;
    
    @Column(name = "change_reason", length = 200)
    private String changeReason; // "MANUAL", "AUTOMATION", "SCHEDULED"
    
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy; // User email or system component
    
    @Column(name = "automation_rule_id")
    private Long automationRuleId;
    
    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "location", length = 100)
    private String location;
    
    public DeviceStateLog() {
        this.logTime = LocalDateTime.now();
    }
    
    public DeviceStateLog(SecureDevice device, SecureUser owner, String previousState, String newState) {
        this();
        this.device = device;
        this.owner = owner;
        this.previousState = previousState;
        this.newState = newState;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SecureDevice getDevice() { return device; }
    public void setDevice(SecureDevice device) { this.device = device; }
    
    public SecureUser getOwner() { return owner; }
    public void setOwner(SecureUser owner) { this.owner = owner; }
    
    public String getPreviousState() { return previousState; }
    public void setPreviousState(String previousState) { this.previousState = previousState; }
    
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    
    public String getStateValue() { return stateValue; }
    public void setStateValue(String stateValue) { this.stateValue = stateValue; }
    
    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }
    
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    
    public Long getAutomationRuleId() { return automationRuleId; }
    public void setAutomationRuleId(Long automationRuleId) { this.automationRuleId = automationRuleId; }
    
    public LocalDateTime getLogTime() { return logTime; }
    public void setLogTime(LocalDateTime logTime) { this.logTime = logTime; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    // Business methods
    public boolean isStateChanged() {
        return previousState != null && !previousState.equals(newState);
    }
    
    public String getStateTransition() {
        return (previousState != null ? previousState : "UNKNOWN") + " → " + newState;
    }
    
    @Override
    public String toString() {
        return "DeviceStateLog{" +
                "id=" + id +
                ", deviceId=" + (device != null ? device.getId() : "null") +
                ", ownerEmail=" + (owner != null ? owner.getEmail() : "null") +
                ", transition='" + getStateTransition() + '\'' +
                ", changeReason='" + changeReason + '\'' +
                ", triggeredBy='" + triggeredBy + '\'' +
                ", logTime=" + logTime +
                '}';
    }
}