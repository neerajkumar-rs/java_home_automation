package com.example.homeautomation.automation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutomationRuleDataInitializer {

    @Bean
    CommandLineRunner initializeAutomationRules(AutomationRuleService automationRuleService) {
        return args -> automationRuleService.initialize();
    }
}