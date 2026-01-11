package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditLogger auditLogger() {
        return new AuditLogger();
    }

    // Placeholder classes for demo
    public static class PasswordEncoder {
        public String encode(String password) { return password; }
    }

    public static class BCryptPasswordEncoder extends PasswordEncoder {
        @Override
        public String encode(String password) { return "bcrypt:" + password; }
    }

    public static class AuditLogger {
        public void log(String action) { System.out.println("AUDIT: " + action); }
    }
}
