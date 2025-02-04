package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
final class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    @Autowired
    private AnnouncementService announcementService;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void perform() {
        LOGGER.info("Running scheduled task at {}", new java.util.Date());
        announcementService.getAnnouncements().forEach(a -> {
            LOGGER.info("{}: {} {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(a.publishTime()), ZoneId.systemDefault()),
                    a.description(), a.title());
        });
    }
}
