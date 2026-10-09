-- Seed data for Home Automation System
-- This file is run after schema.sql

-- =========== USERS ===========
-- NOTE: Passwords are hashed with BCrypt. The hash for 'admin123' and 'user123'
INSERT INTO users (username, email, password, first_name, last_name, active) VALUES
('admin', 'admin@homeautomation.com', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'Admin', 'User', 1),
('user', 'user@homeautomation.com', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'Regular', 'User', 1);

-- User roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(2, 'ROLE_USER');

-- =========== DEVICES ===========
-- Admin owns all devices initially
INSERT INTO devices (name, state, control_type, sensor_type, active, value, room) VALUES
('Living Room Light', 'OFF', 'SWITCH', NULL, 1, 100, 'Living Room'),
('Bedroom Lamp', 'OFF', 'SLIDER', NULL, 1, 50, 'Bedroom'),
('Kitchen RGB Lights', 'OFF', 'RGB', NULL, 1, 0, 'Kitchen'),
('Temperature Sensor', 'ON', 'SENSOR', 'temperature', 1, 22, 'Living Room'),
('Motion Sensor', 'ON', 'SENSOR', 'motion', 1, 0, 'Hallway'),
('Ceiling Fan', 'OFF', 'SLIDER', NULL, 1, 0, 'Bedroom'),
('Air Conditioner', 'OFF', 'SLIDER', NULL, 1, 0, 'Living Room'),
('Bedroom Night Light', 'OFF', 'SWITCH', NULL, 0, 0, 'Bedroom'); -- Inactive device

-- User-device assignments (admin can control all, user can control some)
INSERT INTO user_devices (user_id, device_id, can_control) VALUES
-- Admin can control all devices
(1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 4, 1), (1, 5, 1), (1, 6, 1), (1, 7, 1), (1, 8, 1),
-- User can control only these devices
(2, 1, 1), -- Living Room Light
(2, 2, 1), -- Bedroom Lamp
(2, 4, 0), -- Temperature Sensor (read-only/no control)
(2, 5, 0); -- Motion Sensor (read-only/no control)

-- =========== AUTOMATION RULES ===========
-- Time-based: Turn on living room light at 6 PM
INSERT INTO automation_rules (name, trigger_device_id, trigger_type, trigger_time, action_device_id, action_type, action_value, created_by, enabled) VALUES
('Evening Lights', 4, 'TIME', '18:00:00', 1, 'ON', NULL, 1, 1);

-- Sensor-based: When temperature > 25°C, turn on AC
INSERT INTO automation_rules (name, trigger_device_id, trigger_type, sensor_condition, sensor_threshold, action_device_id, action_type, action_value, created_by, enabled) VALUES
('Cooling System', 4, 'SENSOR', '>', 25.0, 7, 'ON', 22, 1, 1);

-- State-based: When motion detected, turn on hallway light
INSERT INTO automation_rules (name, trigger_device_id, trigger_type, action_device_id, action_type, created_by, enabled) VALUES
('Motion Lights', 5, 'DEVICE_STATE', 1, 'ON', 1, 1);

-- =========== INITIAL LOGS ===========
INSERT INTO device_log (device_id, user_id, old_state, new_state, old_value, new_value, action_type) VALUES
(1, 1, NULL, 'OFF', NULL, 100, 'INITIAL'),
(2, 1, NULL, 'OFF', NULL, 50, 'INITIAL'),
(3, 1, NULL, 'OFF', NULL, 0, 'INITIAL'),
(4, 1, NULL, 'ON', NULL, 22, 'INITIAL'),
(5, 1, NULL, 'ON', NULL, 0, 'INITIAL');

INSERT INTO automation_log (rule_id, result, details) VALUES
(1, 'SCHEDULED', 'Rule scheduled for execution'),
(2, 'ACTIVE', 'Waiting for temperature threshold'),
(3, 'ACTIVE', 'Waiting for motion detection');