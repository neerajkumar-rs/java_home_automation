-- ============================================================================
-- FINAL PRODUCTION-SECURE SCHEMA
-- ============================================================================
-- Core Requirements:
-- 1. Email as primary key for users
-- 2. Strict device isolation via email foreign key
-- 3. Zero visibility between users' devices
-- ============================================================================

-- Disable foreign keys during creation
PRAGMA foreign_keys = OFF;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS device_state_log;
DROP TABLE IF EXISTS user_activity_log;
DROP TABLE IF EXISTS device_shares;
DROP TABLE IF EXISTS user_roles_old;
DROP TABLE IF EXISTS users_old;
DROP TABLE IF EXISTS devices_old;

-- ============================================================================
-- CORE TABLES
-- ============================================================================

-- USERS TABLE (Email as PRIMARY KEY)
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT chk_valid_email CHECK (email LIKE '%@%.%')
);

-- USER ROLES (Links to email)
CREATE TABLE user_roles (
    user_email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    
    PRIMARY KEY (user_email, role),
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    
    CONSTRAINT chk_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

-- DEVICES TABLE (STRICT ISOLATION VIA EMAIL)
CREATE TABLE devices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_uid VARCHAR(64) UNIQUE NOT NULL,
    
    -- CRITICAL: Ownership via email foreign key
    owner_email VARCHAR(255) NOT NULL,
    
    -- Device information
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subtype VARCHAR(50),
    room VARCHAR(50),
    
    -- State
    state VARCHAR(3) DEFAULT 'OFF' NOT NULL,
    value INTEGER DEFAULT 0 NOT NULL,
    red INTEGER DEFAULT 255 NOT NULL,
    green INTEGER DEFAULT 255 NOT NULL,
    blue INTEGER DEFAULT 255 NOT NULL,
    
    -- Status
    online BOOLEAN DEFAULT 0 NOT NULL,
    active BOOLEAN DEFAULT 1 NOT NULL,
    last_communication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- FOREIGN KEY ENFORCEMENT (Core security)
    FOREIGN KEY (owner_email) REFERENCES users(email) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Business constraints
    CONSTRAINT chk_device_type CHECK (type IN ('SWITCH', 'LIGHT', 'THERMOSTAT', 'SENSOR', 'LOCK', 'CAMERA', 'OTHER')),
    CONSTRAINT chk_state CHECK (state IN ('ON', 'OFF', 'ERR')),
    CONSTRAINT uniq_owner_device_name UNIQUE (owner_email, name)
);

-- ============================================================================
-- ENHANCED SECURITY TABLES
-- ============================================================================

-- DEVICE SHARES (Controlled sharing with permissions)
CREATE TABLE device_shares (
    device_id INTEGER NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    shared_with_email VARCHAR(255) NOT NULL,
    
    -- Permissions
    can_view BOOLEAN DEFAULT 1 NOT NULL,
    can_control BOOLEAN DEFAULT 0 NOT NULL,
    can_edit BOOLEAN DEFAULT 0 NOT NULL,
    
    -- Constraints
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT 1 NOT NULL,
    
    -- Audit
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    
    -- Primary key
    PRIMARY KEY (device_id, shared_with_email),
    
    -- Foreign keys (MUST include owner_email for security)
    FOREIGN KEY (device_id, owner_email) REFERENCES devices(id, owner_email) 
        ON DELETE CASCADE,
    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (shared_with_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Business logic
    CONSTRAINT chk_not_self_share CHECK (owner_email != shared_with_email)
);

-- AUDIT LOGS (Security essential)
CREATE TABLE device_state_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Device reference (with owner for security)
    device_id INTEGER NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    
    -- State change
    previous_state VARCHAR(3),
    new_state VARCHAR(3),
    previous_value INTEGER,
    new_value INTEGER,
    
    -- Context
    changed_by_email VARCHAR(255),
    change_source VARCHAR(20) NOT NULL,
    
    -- Timestamp
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Foreign keys
    FOREIGN KEY (device_id, owner_email) REFERENCES devices(id, owner_email) 
        ON DELETE CASCADE,
    FOREIGN KEY (changed_by_email) REFERENCES users(email) ON DELETE SET NULL ON UPDATE CASCADE,
    
    CONSTRAINT chk_change_source CHECK (change_source IN ('USER', 'AUTOMATION', 'DEVICE', 'SYSTEM'))
);

CREATE TABLE user_activity_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- User reference
    user_email VARCHAR(255) NOT NULL,
    
    -- Activity
    activity_type VARCHAR(50) NOT NULL,
    activity_target VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Status
    success BOOLEAN NOT NULL,
    error_message TEXT,
    
    -- Timestamp
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
-- PERFORMANCE INDEXES (CRITICAL FOR SECURITY)
-- ============================================================================

-- Users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active) WHERE active = 1;

-- Devices table (MOST IMPORTANT FOR ISOLATION)
CREATE INDEX idx_devices_owner ON devices(owner_email);
CREATE INDEX idx_devices_type ON devices(type);
CREATE INDEX idx_devices_state ON devices(state);
CREATE INDEX idx_devices_online ON devices(online) WHERE online = 1;
CREATE INDEX idx_devices_room ON devices(room) WHERE room IS NOT NULL;

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
-- DATA INTEGRITY TRIGGERS
-- ============================================================================

-- Log device state changes automatically
CREATE TRIGGER trg_device_state_change 
AFTER UPDATE OF state, value ON devices
FOR EACH ROW
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
        'AUTOMATIC', NEW.owner_email
    );
END;

-- ============================================================================
-- DEFAULT DATA
-- ============================================================================

-- Admin user (same credentials as old system for compatibility)
INSERT OR IGNORE INTO users (email, username, password, first_name, last_name, active) VALUES
('admin@homeautomation.com', 'admin', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'System', 'Administrator', 1);

-- Regular user
INSERT OR IGNORE INTO users (email, username, password, first_name, last_name, active) VALUES
('user@homeautomation.com', 'user', '$2a$10$TIfkuQ8D0WuxZdooBQj.oOloGvkKL3Wc4afUsWuxKBMaSJhdiFhF6', 'Regular', 'User', 1);

-- Roles
INSERT OR IGNORE INTO user_roles (user_email, role) VALUES
('admin@homeautomation.com', 'ROLE_ADMIN'),
('admin@homeautomation.com', 'ROLE_USER'),
('user@homeautomation.com', 'ROLE_USER');

-- Sample devices
INSERT OR IGNORE INTO devices (device_uid, owner_email, name, type, room, state, value) VALUES
('LIGHT001', 'admin@homeautomation.com', 'Living Room Light', 'LIGHT', 'Living Room', 'OFF', 100),
('SWITCH001', 'admin@homeautomation.com', 'Bedroom Switch', 'SWITCH', 'Bedroom', 'OFF', 0),
('SENSOR001', 'admin@homeautomation.com', 'Temperature Sensor', 'SENSOR', 'Living Room', 'ON', 22);

-- Share a device with regular user
INSERT OR IGNORE INTO device_shares (device_id, owner_email, shared_with_email, can_view, can_control) 
SELECT d.id, d.owner_email, 'user@homeautomation.com', 1, 1
FROM devices d WHERE d.name = 'Living Room Light';

-- ============================================================================
-- SECURITY VIEWS FOR DATA ISOLATION
-- ============================================================================

-- View template for user's accessible devices
-- Note: Applications should use parameterized queries with user_email parameter

CREATE VIEW v_user_devices AS
SELECT 
    d.*,
    CASE 
        WHEN d.owner_email = '{user_email}' THEN 'OWNER'
        WHEN EXISTS (
            SELECT 1 FROM device_shares ds 
            WHERE ds.device_id = d.id 
            AND ds.shared_with_email = '{user_email}'
            AND ds.active = 1
            AND (ds.expires_at IS NULL OR ds.expires_at > CURRENT_TIMESTAMP)
        ) THEN 'SHARED'
        ELSE 'NONE'
    END as access_level,
    COALESCE((
        SELECT ds.can_control 
        FROM device_shares ds 
        WHERE ds.device_id = d.id 
        AND ds.shared_with_email = '{user_email}'
        AND ds.active = 1
        AND (ds.expires_at IS NULL OR ds.expires_at > CURRENT_TIMESTAMP)
        LIMIT 1
    ), 0) as can_control_shared
FROM devices d
WHERE d.owner_email = '{user_email}' 
   OR EXISTS (
        SELECT 1 FROM device_shares ds 
        WHERE ds.device_id = d.id 
        AND ds.shared_with_email = '{user_email}'
        AND ds.active = 1
        AND (ds.expires_at IS NULL OR ds.expires_at > CURRENT_TIMESTAMP)
   );

-- ============================================================================
-- PORTED COMPATIBILITY VIEWS
-- ============================================================================

-- For backward compatibility with existing code

CREATE VIEW v_all_devices_compat AS
SELECT 
    d.id,
    d.name,
    d.type,
    d.state,
    d.value,
    d.red,
    d.green,
    d.blue,
    d.online as online_status,
    d.created_at as added_time
FROM devices d;

-- ============================================================================
-- ENABLE FOREIGN KEYS AND EXIT
-- ============================================================================

PRAGMA foreign_keys = ON;

-- ============================================================================
-- QUERY EXAMPLES WITH SECURITY ISOLATION
-- ============================================================================

/*
EXAMPLE 1: Get all devices for specific user (CRITICAL - Always filter by email)
   SELECT * FROM devices WHERE owner_email = 'user@example.com';

EXAMPLE 2: Get device by ID for specific user (PREVENTS CROSS-USER ACCESS)
   SELECT * FROM devices 
   WHERE id = 123 AND owner_email = 'user@example.com';

EXAMPLE 3: Count devices by type for user
   SELECT type, COUNT(*) 
   FROM devices 
   WHERE owner_email = 'user@example.com'
   GROUP BY type;

EXAMPLE 4: Get shared devices
   SELECT d.* 
   FROM devices d
   JOIN device_shares ds ON d.id = ds.device_id
   WHERE ds.shared_with_email = 'user@example.com'
   AND ds.active = 1;

EXAMPLE 5: Check if user has access to device
   SELECT CASE 
       WHEN owner_email = 'user@example.com' THEN 1
       WHEN EXISTS (
           SELECT 1 FROM device_shares ds 
           WHERE ds.device_id = :deviceId 
           AND ds.shared_with_email = 'user@example.com'
           AND ds.active = 1
       ) THEN 1
       ELSE 0
   END as has_access
   FROM devices WHERE id = :deviceId;
*/

-- ============================================================================
-- VERIFICATION QUERY
-- ============================================================================

SELECT 'SCHEMA CREATED SUCCESSFULLY' as status;

SELECT 'Users:' as category, COUNT(*) as count FROM users
UNION ALL
SELECT 'Devices:' as category, COUNT(*) as count FROM devices
UNION ALL
SELECT 'Device Shares:' as category, COUNT(*) as count FROM device_shares
UNION ALL
SELECT 'Indexes:' as category, COUNT(*) as count FROM sqlite_master WHERE type = 'index';