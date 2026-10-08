package com.example.homeautomation.automation;

public class AutomationRule {

    private Long id;
    private Long triggerDeviceId;
    private String triggerType;
    private Double threshold;
    private Long actionDeviceId;
    private String actionType;
    private boolean active;

    public AutomationRule(Long id, Long triggerDeviceId, String triggerType,
                          Double threshold, Long actionDeviceId,
                          String actionType, boolean active) {
        this.id = id;
        this.triggerDeviceId = triggerDeviceId;
        this.triggerType = triggerType;
        this.threshold = threshold;
        this.actionDeviceId = actionDeviceId;
        this.actionType = actionType;
        this.active = active;
    }

    public AutomationRule(Long triggerDeviceId, String triggerType,
                          Double threshold, Long actionDeviceId,
                          String actionType) {
        this(null, triggerDeviceId, triggerType, threshold,
             actionDeviceId, actionType, true);
    }

    public Long getId() {
        return id;
    }

    public Long getTriggerDeviceId() {
        return triggerDeviceId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public Double getThreshold() {
        return threshold;
    }

    public Long getActionDeviceId() {
        return actionDeviceId;
    }

    public String getActionType() {
        return actionType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}