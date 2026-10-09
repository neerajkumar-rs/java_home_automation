package com.example.homeautomation.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Secure Automation Rule entity 
 */
@Entity
@Table(name = "secure_automation_rules")
public class SecureAutomationRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_email", referencedColumnName = "email", nullable = false)
    private SecureUser owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private SecureDevice device;
    
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;
    
    @Column(name = "rule_description", length = 500)
    private String ruleDescription;
    
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType; // "TIME_BASED", "DEVICE_BASED", "EVENT_BASED"
    
    @Column(name = "trigger_time")
    private LocalTime triggerTime;
    
    @Column(name = "trigger_condition", length = 500)
    private String triggerCondition;
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // "TURN_ON", "TURN_OFF", "SET_VALUE"
    
    @Column(name = "action_value")
    private String actionValue;
    
    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek; // "MON,TUE,WED,THU,FRI,SAT,SUN"
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @Column(name = "last_executed")
    private LocalDateTime lastExecuted;
    
    @Column(name = "execution_count")
    private int executionCount = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public SecureAutomationRule() {
        this.createdAt = LocalDateTime.now();
    }
    
    public SecureAutomationRule(SecureUser owner, SecureDevice device, String ruleName, String ruleType, String actionType) {
        this();
        this.owner = owner;
        this.device = device;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.actionType = actionType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SecureUser getOwner() { return owner; }
    public void setOwner(SecureUser owner) { this.owner = owner; }
    
    public SecureDevice getDevice() { return device; }
    public void setDevice(SecureDevice device) { this.device = device; }
    
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    
    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }
    
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    
    public LocalTime getTriggerTime() { return triggerTime; }
    public void setTriggerTime(LocalTime triggerTime) { this.triggerTime = triggerTime; }
    
    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getActionValue() { return actionValue; }
    public void setActionValue(String actionValue) { this.actionValue = actionValue; }
    
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public LocalDateTime getLastExecuted() { return lastExecuted; }
    public void setLastExecuted(LocalDateTime lastExecuted) { this.lastExecuted = lastExecuted; }
    
    public int getExecutionCount() { return executionCount; }
    public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public void incrementExecutionCount() {
        this.executionCount++;
        this.lastExecuted = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SecureAutomationRule{" +
                "id=" + id +
                ", ownerEmail=" + (owner != null ? owner.getEmail() : "null") +
                ", deviceId=" + (device != null ? device.getId() : "null") +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", enabled=" + enabled +
                ", executionCount=" + executionCount +
                '}';
    }
}