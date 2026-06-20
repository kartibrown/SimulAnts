package com.kartibrown.simulants.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Thread pool
        scheduler.setPoolSize(4);

        // Set a name for the thread
        scheduler.setThreadNamePrefix("SimulAnts-Scheduler-");

        scheduler.initialize();
        return scheduler;
    }
}