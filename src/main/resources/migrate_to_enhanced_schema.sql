-- ============================================================================
-- MIGRATION SCRIPT: OLD -> NEW SECURE SCHEMA
-- ============================================================================
-- This script migrates from legacy schema to enhanced secure schema
-- Run this AFTER backing up your database
-- ============================================================================

PRAGMA foreign_keys = OFF;
PRAGMA journal_mode = WAL;
BEGIN TRANSACTION;

-- ============================================================================
-- STEP 1: BACKUP EXISTING DATA
-- ============================================================================

-- Create backup tables
CREATE TABLE users_backup AS SELECT * FROM users;
CREATE TABLE user_roles_backup AS SELECT * FROM user_roles;
CREATE TABLE devices_backup AS SELECT * FROM devices;

-- ============================================================================
-- STEP 2: RECREATE TABLES WITH ENHANCED SCHEMA
-- ============================================================================

-- Drop existing tables (in correct order)
DROP TABLE IF EXISTS device_shares;
DROP TABLE IF EXISTS automation_rules;
DROP TABLE IF EXISTS device_state_log;
DROP TABLE IF EXISTS user_activity_log;
DROP TABLE IF EXISTS device_log;
DROP TABLE IF EXISTS automation_log;
DROP TABLE IF EXISTS user_devices;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS devices;

-- Create enhanced users table (email as PK)
CREATE TABLE users (
    email VARCHAR(255) PRIMARY KEY NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT 1 NOT NULL,
    locked BOOLEAN DEFAULT 0 NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    last_login TIMESTAMP,
    last_password_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_failed_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_valid_email CHECK (email LIKE '%@%.%'),
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3)
);

-- Create enhanced devices table (with owner_email)
CREATE TABLE devices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_uid VARCHAR(64) UNIQUE NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subtype VARCHAR(50),
    room VARCHAR(50),
    state VARCHAR(3) DEFAULT 'OFF' NOT NULL,
    value INTEGER DEFAULT 0 NOT NULL,
    red INTEGER DEFAULT 255 NOT NULL,
    green INTEGER DEFAULT 255 NOT NULL,
    blue INTEGER DEFAULT 255 NOT NULL,
    online BOOLEAN DEFAULT 0 NOT NULL,
    active BOOLEAN DEFAULT 1 NOT NULL,
    last_communication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    capabilities TEXT DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (owner_email) REFERENCES users(email) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_device_type CHECK (type IN ('SWITCH', 'LIGHT', 'THERMOSTAT', 'SENSOR', 'LOCK', 'CAMERA', 'OTHER')),
    CONSTRAINT chk_state CHECK (state IN ('ON', 'OFF', 'ERR')),
    CONSTRAINT uniq_owner_device_name UNIQUE (owner_email, name)
);

-- Create enhanced user_roles table
CREATE TABLE user_roles (
    user_email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_email, role),
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

-- Create device sharing table
CREATE TABLE device_shares (
    device_id INTEGER NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    shared_with_email VARCHAR(255) NOT NULL,
    can_view BOOLEAN DEFAULT 1 NOT NULL,
    can_control BOOLEAN DEFAULT 0 NOT NULL,
    can_edit BOOLEAN DEFAULT 0 NOT NULL,
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT 1 NOT NULL,
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    PRIMARY KEY (device_id, shared_with_email),
    FOREIGN KEY (device_id, owner_email) REFERENCES devices(id, owner_email) ON DELETE CASCADE,
    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (shared_with_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_not_self_share CHECK (owner_email != shared_with_email)
);

-- Create audit logging tables
CREATE TABLE device_state_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id INTEGER NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    previous_state VARCHAR(3),
    new_state VARCHAR(3),
    previous_value INTEGER,
    new_value INTEGER,
    changed_by_email VARCHAR(255),
    change_source VARCHAR(20) NOT NULL,
    automation_rule_id INTEGER,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (device_id, owner_email) REFERENCES devices(id, owner_email) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_email) REFERENCES users(email) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_change_source CHECK (change_source IN ('USER', 'AUTOMATION', 'DEVICE', 'SYSTEM'))
);

CREATE TABLE user_activity_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_email VARCHAR(255) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    activity_target VARCHAR(100),
    activity_details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_activity_type CHECK (activity_type IN (
        'LOGIN', 'LOGOUT', 'LOGIN_FAILED', 'PASSWORD_CHANGE',
        'DEVICE_ADD', 'DEVICE_UPDATE', 'DEVICE_DELETE',
        'AUTOMATION_CREATE', 'AUTOMATION_UPDATE', 'AUTOMATION_DELETE',
        'DEVICE_SHARE', 'PROFILE_UPDATE'
    ))
);

-- ============================================================================
-- STEP 3: MIGRATE DATA
-- ============================================================================

-- Migrate users with email generation
INSERT INTO users (email, username, password, first_name, last_name, active, created_at)
SELECT 
    COALESCE(email, username || '@homeautomation.com') as email,
    username,
    password,
    COALESCE(first_name, 'User') as first_name,
    COALESCE(last_name, 'Unknown') as last_name,
    active,
    created_at
FROM users_backup;

-- Migrate user roles
INSERT INTO user_roles (user_email, role)
SELECT 
    COALESCE(u.email, u.username || '@homeautomation.com') as user_email,
    ur.role
FROM user_roles_backup ur
JOIN users_backup u ON ur.user_id = u.id;

-- Migrate devices with owner assignment
INSERT INTO devices (device_uid, owner_email, name, type, room, state, value, red, green, blue, online, active, created_at)
SELECT 
    -- Generate device UID
    'DEV' || printf('%03d', ROW_NUMBER() OVER (ORDER BY d.id)) as device_uid,
    -- Assign to admin user (for initial migration)
    'admin@homeautomation.com' as owner_email,
    d.name,
    d.type,
    d.room,
    COALESCE(d.state, 'OFF') as state,
    COALESCE(d.value, 0) as value,
    COALESCE(d.red, 255) as red,
    COALESCE(d.green, 255) as green,
    COALESCE(d.blue, 255) as blue,
    0 as online, -- Default to offline during migration
    1 as active,
    CURRENT_TIMESTAMP as created_at
FROM devices_backup d;

-- ============================================================================
-- STEP 4: CREATE INDEXES FOR PERFORMANCE
-- ============================================================================

-- Users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active) WHERE active = 1;

-- Devices table (CRITICAL FOR SECURITY)
CREATE INDEX idx_devices_owner ON devices(owner_email);
CREATE INDEX idx_devices_type ON devices(type);
CREATE INDEX idx_devices_room ON devices(room) WHERE room IS NOT NULL;
CREATE INDEX idx_devices_online ON devices(online) WHERE online = 1;

-- Device shares
CREATE INDEX idx_device_shares_device ON device_shares(device_id);
CREATE INDEX idx_device_shares_shared_with ON device_shares(shared_with_email);
CREATE INDEX idx_device_shares_active ON device_shares(active) WHERE active = 1;

-- Logging tables
CREATE INDEX idx_device_log_device ON device_state_log(device_id);
CREATE INDEX idx_device_log_changed_at ON device_state_log(changed_at DESC);
CREATE INDEX idx_user_log_user ON user_activity_log(user_email);
CREATE INDEX idx_user_log_logged_at ON user_activity_log(logged_at DESC);

-- ============================================================================
-- STEP 5: CREATE TRIGGERS FOR DATA INTEGRITY
-- ============================================================================

-- Update user timestamp
CREATE TRIGGER update_users_timestamp AFTER UPDATE ON users
BEGIN
    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE email = NEW.email;
END;

-- Log device state changes
CREATE TRIGGER log_device_state_change AFTER UPDATE ON devices
WHEN OLD.state != NEW.state OR OLD.value != NEW.value
BEGIN
    INSERT INTO device_state_log (
        device_id, owner_email,
        previous_state, new_state,
        previous_value, new_value,
        change_source, changed_by_email
    ) VALUES (
        NEW.id, NEW.owner_email,
        OLD.state, NEW.state,
        OLD.value, NEW.value,
        'USER', -- Default source
        NEW.owner_email
    );
END;

-- ============================================================================
-- STEP 6: CREATE ADMIN USER (IF NOT EXISTS)
-- ============================================================================

INSERT OR IGNORE INTO users (email, username, password, first_name, last_name, active) VALUES
('admin@homeautomation.com', 'admin', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'Admin', 'User', 1);

INSERT OR IGNORE INTO user_roles (user_email, role) VALUES
('admin@homeautomation.com', 'ROLE_ADMIN'),
('admin@homeautomation.com', 'ROLE_USER');

INSERT OR IGNORE INTO users (email, username, password, first_name, last_name, active) VALUES
('user@homeautomation.com', 'user', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'Regular', 'User', 1);

INSERT OR IGNORE INTO user_roles (user_email, role) VALUES
('user@homeautomation.com', 'ROLE_USER');

-- ============================================================================
-- STEP 7: CREATE SECURITY VIEWS
-- ============================================================================

-- View for authenticated user's devices (including shared ones)
-- Note: This view uses a placeholder for current_user_email
-- In application, use parameterized queries instead

CREATE VIEW user_accessible_devices_template AS
SELECT 
    d.*,
    CASE 
        WHEN d.owner_email = '{current_user_email}' THEN 'OWNER'
        WHEN EXISTS (
            SELECT 1 FROM device_shares ds 
            WHERE ds.device_id = d.id 
            AND ds.shared_with_email = '{current_user_email}'
            AND ds.active = 1
            AND (ds.expires_at IS NULL OR ds.expires_at > CURRENT_TIMESTAMP)
        ) THEN 'SHARED'
        ELSE 'NONE'
    END as access_level
FROM devices d
WHERE d.owner_email = '{current_user_email}' 
   OR EXISTS (
        SELECT 1 FROM device_shares ds 
        WHERE ds.device_id = d.id 
        AND ds.shared_with_email = '{current_user_email}'
        AND ds.active = 1
        AND (ds.expires_at IS NULL OR ds.expires_at > CURRENT_TIMESTAMP)
   );

-- ============================================================================
-- STEP 8: CLEANUP AND VERIFICATION
-- ============================================================================

-- Drop backup tables
DROP TABLE IF EXISTS users_backup;
DROP TABLE IF EXISTS user_roles_backup;
DROP TABLE IF EXISTS devices_backup;

-- Verify migration
SELECT 'Users migrated:' as check, COUNT(*) FROM users;
SELECT 'Roles migrated:' as check, COUNT(*) FROM user_roles;
SELECT 'Devices migrated:' as check, COUNT(*) FROM devices;
SELECT 'Indexes created:' as check, COUNT(*) FROM sqlite_master WHERE type = 'index';

-- ============================================================================
-- STEP 9: COMMIT AND RESET
-- ============================================================================

COMMIT;
PRAGMA foreign_keys = ON;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================

PRINT 'Migration completed successfully!';
PRINT 'Please update your application.properties to use the new schema.';
PRINT 'Key changes:';
PRINT '  - Email is now primary key for users';
PRINT '  - All devices have owner_email foreign key';
PRINT '  - Enhanced security with device sharing';
PRINT '  - Complete audit logging';
PRINT '';
PRINT 'Default credentials:';
PRINT '  Admin: admin@homeautomation.com / admin123';
PRINT '  User:  user@homeautomation.com / user123';