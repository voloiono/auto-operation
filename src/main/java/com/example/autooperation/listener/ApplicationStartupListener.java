package com.example.autooperation.listener;

import com.example.autooperation.service.ScheduleExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener {
    private final ScheduleExecutorService scheduleExecutorService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            log.info("Initializing scheduled tasks...");
            scheduleExecutorService.initializeSchedules();
            log.info("Scheduled tasks initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize scheduled tasks", e);
        }
    }
}
