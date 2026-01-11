package com.example.demo.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        System.out.println("Cleaning up expired sessions...");
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyReport() {
        System.out.println("Generating daily report...");
    }
}
