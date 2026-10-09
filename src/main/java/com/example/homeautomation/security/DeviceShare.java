package com.example.homeautomation.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Device Share entity for controlled sharing with permissions
 */
@Entity
@Table(name = "device_shares")
public class DeviceShare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private SecureDevice device;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_email", referencedColumnName = "email", nullable = false)
    private SecureUser owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_email", referencedColumnName = "email", nullable = false)
    private SecureUser sharedWith;
    
    @Column(name = "can_view", nullable = false)
    private boolean canView = true;
    
    @Column(name = "can_control", nullable = false)
    private boolean canControl = false;
    
    @Column(name = "can_configure", nullable = false)
    private boolean canConfigure = false;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "shared_at", nullable = false)
    private LocalDateTime sharedAt;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    public DeviceShare() {
        this.sharedAt = LocalDateTime.now();
    }
    
    public DeviceShare(SecureDevice device, SecureUser owner, SecureUser sharedWith) {
        this();
        this.device = device;
        this.owner = owner;
        this.sharedWith = sharedWith;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SecureDevice getDevice() { return device; }
    public void setDevice(SecureDevice device) { this.device = device; }
    
    public SecureUser getOwner() { return owner; }
    public void setOwner(SecureUser owner) { this.owner = owner; }
    
    public SecureUser getSharedWith() { return sharedWith; }
    public void setSharedWith(SecureUser sharedWith) { this.sharedWith = sharedWith; }
    
    public boolean isCanView() { return canView; }
    public void setCanView(boolean canView) { this.canView = canView; }
    
    public boolean isCanControl() { return canControl; }
    public void setCanControl(boolean canControl) { this.canControl = canControl; }
    
    public boolean isCanConfigure() { return canConfigure; }
    public void setCanConfigure(boolean canConfigure) { this.canConfigure = canConfigure; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
    
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    
    // Business methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
    
    public void revoke() {
        this.active = false;
        this.revokedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "DeviceShare{" +
                "id=" + id +
                ", deviceId=" + (device != null ? device.getId() : "null") +
                ", ownerEmail=" + (owner != null ? owner.getEmail() : "null") +
                ", sharedWithEmail=" + (sharedWith != null ? sharedWith.getEmail() : "null") +
                ", canView=" + canView +
                ", canControl=" + canControl +
                ", active=" + active +
                '}';
    }
}