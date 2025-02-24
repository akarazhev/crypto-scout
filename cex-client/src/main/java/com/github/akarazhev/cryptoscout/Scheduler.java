package com.github.akarazhev.cryptoscout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
final class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final EventSource bybitEventSource;
    private final EventPublisher eventPublisher;

    public Scheduler(@Qualifier("bybitEventSource") final EventSource bybitEventSource, final EventPublisher eventPublisher) {
        this.bybitEventSource = bybitEventSource;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void perform() {
        final var published = new AtomicInteger(0);
        bybitEventSource.getEvents().forEach(event -> {
            eventPublisher.publish(event);
            published.incrementAndGet();
        });
        LOGGER.info("Running scheduled task published {} events", published);
    }
}
