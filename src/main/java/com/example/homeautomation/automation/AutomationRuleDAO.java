package com.example.homeautomation.automation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.homeautomation.jdbc.DBConnection;

@Repository
public class AutomationRuleDAO {

    private final DBConnection dbConnection;

    public AutomationRuleDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS automation_rules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trigger_device_id INTEGER NOT NULL,
                    trigger_type TEXT NOT NULL,
                    threshold REAL,
                    action_device_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                )
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    public List<AutomationRule> findAll() throws SQLException {
        List<AutomationRule> rules = new ArrayList<>();

        String sql = """
                SELECT id, trigger_device_id, trigger_type, threshold,
                       action_device_id, action_type, active
                FROM automation_rules
                ORDER BY id
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rules.add(new AutomationRule(
                        resultSet.getLong("id"),
                        resultSet.getLong("trigger_device_id"),
                        resultSet.getString("trigger_type"),
                        resultSet.getObject("threshold", Double.class),
                        resultSet.getLong("action_device_id"),
                        resultSet.getString("action_type"),
                        resultSet.getInt("active") == 1
                ));
            }
        }

        return rules;
    }

    public AutomationRule create(AutomationRule rule) throws SQLException {
        String sql = """
                INSERT INTO automation_rules
                (trigger_device_id, trigger_type, threshold,
                 action_device_id, action_type, active)
                VALUES (?, ?, ?, ?, ?, 1)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, rule.getTriggerDeviceId());
            statement.setString(2, rule.getTriggerType());

            if (rule.getThreshold() == null) {
                statement.setNull(3, java.sql.Types.REAL);
            } else {
                statement.setDouble(3, rule.getThreshold());
            }

            statement.setLong(4, rule.getActionDeviceId());
            statement.setString(5, rule.getActionType());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new AutomationRule(
                            keys.getLong(1),
                            rule.getTriggerDeviceId(),
                            rule.getTriggerType(),
                            rule.getThreshold(),
                            rule.getActionDeviceId(),
                            rule.getActionType(),
                            true
                    );
                }
            }
        }

        throw new SQLException("Unable to create automation rule");
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM automation_rules WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}