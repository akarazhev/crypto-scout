package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.AnnouncementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
final class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    @Autowired
    private AnnouncementService announcementService;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void perform() {
        LOGGER.info("Scheduled task executed at {} and received: {}", new java.util.Date(),
                announcementService.getAnnouncements().count());
    }
}
