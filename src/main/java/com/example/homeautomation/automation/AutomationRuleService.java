package com.example.homeautomation.automation;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AutomationRuleService {

    private final AutomationRuleDAO automationRuleDAO;

    public AutomationRuleService(AutomationRuleDAO automationRuleDAO) {
        this.automationRuleDAO = automationRuleDAO;
    }

    public void initialize() throws SQLException {
        automationRuleDAO.createTable();
    }

    public List<AutomationRule> findAll() throws SQLException {
        return automationRuleDAO.findAll();
    }

    public AutomationRule create(AutomationRule rule) throws SQLException {
        if (rule.getTriggerDeviceId() == null) {
            throw new IllegalArgumentException("Trigger device is required");
        }

        if (rule.getActionDeviceId() == null) {
            throw new IllegalArgumentException("Action device is required");
        }

        if (rule.getTriggerType() == null || rule.getTriggerType().isBlank()) {
            throw new IllegalArgumentException("Trigger type is required");
        }

        if (rule.getActionType() == null || rule.getActionType().isBlank()) {
            throw new IllegalArgumentException("Action type is required");
        }

        return automationRuleDAO.create(rule);
    }

    public void delete(Long id) throws SQLException {
        automationRuleDAO.delete(id);
    }
}