package com.example.homeautomation.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecureDeviceRepository extends JpaRepository<SecureDevice, Long> {
    
    // CRITICAL SECURITY: All queries must filter by ownerEmail
    
    // Find all devices owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.owner.email = :ownerEmail")
    List<SecureDevice> findAllByOwnerEmail(@Param("ownerEmail") String ownerEmail);
    
    // Find device by ID only if owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.id = :deviceId AND d.owner.email = :ownerEmail")
    Optional<SecureDevice> findByIdAndOwnerEmail(@Param("deviceId") Long deviceId, 
                                                 @Param("ownerEmail") String ownerEmail);
    
    // Find device by UID only if owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.deviceUid = :deviceUid AND d.owner.email = :ownerEmail")
    Optional<SecureDevice> findByDeviceUidAndOwnerEmail(@Param("deviceUid") String deviceUid, 
                                                        @Param("ownerEmail") String ownerEmail);
    
    // Find devices in room owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.room = :room AND d.owner.email = :ownerEmail")
    List<SecureDevice> findByRoomAndOwnerEmail(@Param("room") String room, 
                                               @Param("ownerEmail") String ownerEmail);
    
    // Find active devices owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.active = true AND d.owner.email = :ownerEmail")
    List<SecureDevice> findActiveByOwnerEmail(@Param("ownerEmail") String ownerEmail);
    
    // Find online devices owned by user
    @Query("SELECT d FROM SecureDevice d WHERE d.online = true AND d.owner.email = :ownerEmail")
    List<SecureDevice> findOnlineByOwnerEmail(@Param("ownerEmail") String ownerEmail);
    
    // Count devices by type for user
    @Query("SELECT d.type, COUNT(d) FROM SecureDevice d WHERE d.owner.email = :ownerEmail GROUP BY d.type")
    List<Object[]> countByTypeAndOwnerEmail(@Param("ownerEmail") String ownerEmail);
    
    // Find shared devices accessible to user (owned OR shared)
    @Query("SELECT d FROM SecureDevice d WHERE " +
           "d.owner.email = :userEmail OR " +
           "EXISTS (SELECT 1 FROM d.shares s WHERE s.sharedWith.email = :userEmail AND s.active = true)")
    List<SecureDevice> findAllAccessibleDevices(@Param("userEmail") String userEmail);
    
    // Find devices shared with specific user
    @Query("SELECT d FROM SecureDevice d WHERE " +
           "EXISTS (SELECT 1 FROM d.shares s WHERE s.sharedWith.email = :userEmail AND s.active = true)")
    List<SecureDevice> findSharedWithUser(@Param("userEmail") String userEmail);
    
    // Check if user has access to device (owner or shared)
    @Query("SELECT COUNT(d) > 0 FROM SecureDevice d WHERE d.id = :deviceId AND " +
           "(d.owner.email = :userEmail OR " +
           "EXISTS (SELECT 1 FROM d.shares s WHERE s.sharedWith.email = :userEmail AND s.active = true))")
    boolean hasAccess(@Param("deviceId") Long deviceId, @Param("userEmail") String userEmail);
    
    // Check if user can control device
    @Query("SELECT COUNT(d) > 0 FROM SecureDevice d WHERE d.id = :deviceId AND " +
           "(d.owner.email = :userEmail OR " +
           "EXISTS (SELECT 1 FROM d.shares s WHERE s.sharedWith.email = :userEmail AND s.canControl = true AND s.active = true))")
    boolean canControl(@Param("deviceId") Long deviceId, @Param("userEmail") String userEmail);
    
    // Search devices by name (only accessible ones)
    @Query("SELECT d FROM SecureDevice d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
           "(d.owner.email = :userEmail OR " +
           "EXISTS (SELECT 1 FROM d.shares s WHERE s.sharedWith.email = :userEmail AND s.active = true))")
    List<SecureDevice> searchAccessibleDevices(@Param("userEmail") String userEmail, 
                                              @Param("searchTerm") String searchTerm);
}