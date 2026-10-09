package com.example.homeautomation.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/like")
@CrossOrigin(origins = "*")
public class LikeController {

    private final Map<Long, Integer> likeCounts = new HashMap<>();

    @PostMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable Long deviceId) {
        int currentLikes = likeCounts.getOrDefault(deviceId, 0);
        likeCounts.put(deviceId, currentLikes + 1);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceId", deviceId);
        response.put("likes", currentLikes + 1);
        response.put("message", "Like added successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> getLikes(@PathVariable Long deviceId) {
        int likes = likeCounts.getOrDefault(deviceId, 0);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceId", deviceId);
        response.put("likes", likes);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> removeLike(@PathVariable Long deviceId) {
        int currentLikes = likeCounts.getOrDefault(deviceId, 0);
        if (currentLikes > 0) {
            likeCounts.put(deviceId, currentLikes - 1);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceId", deviceId);
        response.put("likes", Math.max(0, currentLikes - 1));
        response.put("message", "Like removed successfully");
        
        return ResponseEntity.ok(response);
    }
}