-- Home Automation Database Schema
-- This file is run automatically by Hibernate/JPA on startup

-- Users table
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT 1 NOT NULL,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles (many-to-many)
CREATE TABLE user_roles (
    user_id INTEGER NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Devices table
DROP TABLE IF EXISTS devices;
CREATE TABLE devices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    state VARCHAR(3) DEFAULT 'OFF' NOT NULL,
    control_type VARCHAR(12) DEFAULT 'SWITCH' NOT NULL,
    sensor_type VARCHAR(20),
    active BOOLEAN DEFAULT 1 NOT NULL,
    value INTEGER DEFAULT 0 NOT NULL,
    red INTEGER DEFAULT 255 NOT NULL,
    green INTEGER DEFAULT 255 NOT NULL,
    blue INTEGER DEFAULT 255 NOT NULL,
    room VARCHAR(50),
    owner_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

-- User-Device assignments (which users can control which devices)
DROP TABLE IF EXISTS user_devices;
CREATE TABLE user_devices (
    user_id INTEGER NOT NULL,
    device_id INTEGER NOT NULL,
    can_control BOOLEAN DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Automation rules table
DROP TABLE IF EXISTS automation_rules;
CREATE TABLE automation_rules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    trigger_device_id INTEGER NOT NULL,
    trigger_type VARCHAR(20) NOT NULL, -- 'TIME', 'SENSOR', 'DEVICE_STATE'
    trigger_time TIME,
    sensor_condition VARCHAR(10), -- '>', '<', '=', '>=', '<='
    sensor_threshold REAL,
    action_device_id INTEGER NOT NULL,
    action_type VARCHAR(20) NOT NULL, -- 'ON', 'OFF', 'SET_VALUE', 'SET_COLOR'
    action_value INTEGER,
    action_color VARCHAR(7), -- Hex color like '#FF0000'
    enabled BOOLEAN DEFAULT 1 NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trigger_device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (action_device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Automation execution log
DROP TABLE IF EXISTS automation_log;
CREATE TABLE automation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id INTEGER NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(50),
    details TEXT,
    FOREIGN KEY (rule_id) REFERENCES automation_rules(id) ON DELETE CASCADE
);

-- Device state change log
DROP TABLE IF EXISTS device_log;
CREATE TABLE device_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id INTEGER NOT NULL,
    user_id INTEGER,
    old_state VARCHAR(3),
    new_state VARCHAR(3),
    old_value INTEGER,
    new_value INTEGER,
    action_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_devices_owner ON devices(owner_id);
CREATE INDEX idx_user_devices_user ON user_devices(user_id);
CREATE INDEX idx_user_devices_device ON user_devices(device_id);
CREATE INDEX idx_automation_rules_enabled ON automation_rules(enabled);
CREATE INDEX idx_automation_rules_trigger ON automation_rules(trigger_device_id);
CREATE INDEX idx_automation_log_rule ON automation_log(rule_id);
CREATE INDEX idx_device_log_device ON device_log(device_id);
CREATE INDEX idx_device_log_user ON device_log(user_id);create table "device_shares" ("id" integer, "active" boolean not null, "can_configure" boolean not null, "can_control" boolean not null, "can_view" boolean not null, "expires_at" timestamp, "revoked_at" timestamp, "shared_at" timestamp not null, "device_id" bigint not null, "owner_email" varchar(255) not null, "shared_with_email" varchar(255) not null, primary key ("id"));
create table "device_state_logs" ("id" integer, "automation_rule_id" bigint, "change_reason" varchar(200), "ip_address" varchar(45), "location" varchar(100), "log_time" timestamp not null, "new_state" varchar(50) not null, "new_value" varchar(255), "previous_state" varchar(50), "previous_value" varchar(255), "state_value" varchar(255), "triggered_by" varchar(100), "user_agent" varchar(500), "device_id" bigint not null, "owner_email" varchar(255) not null, primary key ("id"));
alter table "devices" add column "capabilities" "TEXT DEFAULT '{}'";
alter table "devices" add column "created_at" timestamp not null;
alter table "devices" add column "device_uid" varchar(64) not null unique;
alter table "devices" add column "last_communication" timestamp;
alter table "devices" add column "online" boolean not null;
alter table "devices" add column "room" varchar(50);
alter table "devices" add column "subtype" varchar(50);
alter table "devices" add column "type" varchar(50) not null;
alter table "devices" add column "owner_email" varchar(255) not null;
create table "secure_automation_rules" ("id" integer, "action_type" varchar(50) not null, "action_value" varchar(255), "created_at" timestamp not null, "days_of_week" varchar(50), "enabled" boolean not null, "execution_count" integer, "last_executed" timestamp, "rule_description" varchar(500), "rule_name" varchar(100) not null, "rule_type" varchar(50) not null, "trigger_condition" varchar(500), "trigger_time" time(6), "updated_at" timestamp, "device_id" bigint not null, "owner_email" varchar(255) not null, primary key ("id"));
alter table "user_roles" add column "user_email" varchar(255) not null;
alter table "users" add column "active" boolean not null;
alter table "users" add column "failed_attempts" integer;
alter table "users" add column "last_failed_login" timestamp;
alter table "users" add column "last_password_change" timestamp;
alter table "users" add column "locked" boolean not null;
alter table "users" add column "updated_at" timestamp not null;
alter table "devices" drop constraint uniq_owner_device_name;
alter table "devices" drop constraint "UK1vfphwt5fxksb7qsliedcmt17";
alter table "users" drop constraint "UKlgkd7iin2rkv9xkrkvdf6do2v";
create table "device_shares" ("id" integer, "active" boolean not null, "can_configure" boolean not null, "can_control" boolean not null, "can_view" boolean not null, "expires_at" timestamp, "revoked_at" timestamp, "shared_at" timestamp not null, "device_id" bigint not null, "owner_email" varchar(255) not null, "shared_with_email" varchar(255) not null, primary key ("id"));
create table "device_state_logs" ("id" integer, "automation_rule_id" bigint, "change_reason" varchar(200), "ip_address" varchar(45), "location" varchar(100), "log_time" timestamp not null, "new_state" varchar(50) not null, "new_value" varchar(255), "previous_state" varchar(50), "previous_value" varchar(255), "state_value" varchar(255), "triggered_by" varchar(100), "user_agent" varchar(500), "device_id" bigint not null, "owner_email" varchar(255) not null, primary key ("id"));
alter table "devices" add column "capabilities" "TEXT DEFAULT '{}'";
alter table "devices" add column "created_at" timestamp not null;
alter table "devices" add column "device_uid" varchar(64) not null unique;
alter table "devices" add column "last_communication" timestamp;
alter table "devices" add column "online" boolean not null;
alter table "devices" add column "room" varchar(50);
alter table "devices" add column "subtype" varchar(50);
alter table "devices" add column "type" varchar(50) not null;
alter table "devices" add column "owner_email" varchar(255) not null;
create table "secure_automation_rules" ("id" integer, "action_type" varchar(50) not null, "action_value" varchar(255), "created_at" timestamp not null, "days_of_week" varchar(50), "enabled" boolean not null, "execution_count" integer, "last_executed" timestamp, "rule_description" varchar(500), "rule_name" varchar(100) not null, "rule_type" varchar(50) not null, "trigger_condition" varchar(500), "trigger_time" time(6), "updated_at" timestamp, "device_id" bigint not null, "owner_email" varchar(255) not null, primary key ("id"));
alter table "user_roles" add column "user_email" varchar(255) not null;
alter table "users" add column "active" boolean not null;
alter table "users" add column "failed_attempts" integer;
alter table "users" add column "last_failed_login" timestamp;
alter table "users" add column "last_password_change" timestamp;
alter table "users" add column "locked" boolean not null;
alter table "users" add column "updated_at" timestamp not null;
alter table "devices" drop constraint uniq_owner_device_name;
alter table "devices" drop constraint "UK1vfphwt5fxksb7qsliedcmt17";
alter table "users" drop constraint "UKlgkd7iin2rkv9xkrkvdf6do2v";
