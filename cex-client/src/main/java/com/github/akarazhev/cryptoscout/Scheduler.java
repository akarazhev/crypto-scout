package com.github.akarazhev.cryptoscout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
final class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final EventSource bybitEventSource;
    private final EventExchange eventExchange;

    public Scheduler(@Qualifier("bybitEventSource") final EventSource bybitEventSource, final EventExchange eventExchange) {
        this.bybitEventSource = bybitEventSource;
        this.eventExchange = eventExchange;
    }

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void perform() {
        LOGGER.info("Running scheduled task at {}", new java.util.Date());
        bybitEventSource.getEvents().forEach(e -> {
            eventExchange.publish(e);
            LOGGER.info(e.toString());
        });
    }
}
