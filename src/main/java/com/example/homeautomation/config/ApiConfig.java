package com.example.homeautomation.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    // API version constants
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    
    // API endpoint groups
    public static final String AUTH_ENDPOINTS = API_BASE_PATH + "/auth/**";
    public static final String DEVICES_ENDPOINTS = API_BASE_PATH + "/devices/**";
    public static final String USERS_ENDPOINTS = API_BASE_PATH + "/users/**";
    public static final String ADMIN_ENDPOINTS = API_BASE_PATH + "/admin/**";
    public static final String AUTOMATION_ENDPOINTS = API_BASE_PATH + "/automation/**";
}