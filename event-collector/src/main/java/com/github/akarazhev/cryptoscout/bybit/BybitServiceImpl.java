package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
class BybitServiceImpl implements BybitService {
    private final BybitEventRepository repository;

    public BybitServiceImpl(final BybitEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public Optional<Long> save(final String eventType, final Announcement announcement) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        if (announcement == null) {
            throw new IllegalArgumentException("Announcement cannot be null");
        }

        if (repository.existsByEventTimeAndPublishTimeAndStartTimeAndEndTime(announcement.dateTimestamp(),
                announcement.publishTime(), announcement.startDateTimestamp(), announcement.endDateTimestamp())) {
            return Optional.empty();
        }

        final var event = new BybitEvent();
        event.setEventType(eventType);
        event.setTitle(announcement.title());
        event.setDescription(announcement.description());
        event.setTags(announcement.tags().toArray(new String[0]));
        event.setUrl(announcement.url());
        event.setEventTime(announcement.dateTimestamp());
        event.setPublishTime(announcement.publishTime());
        event.setStartTime(announcement.startDateTimestamp());
        event.setEndTime(announcement.endDateTimestamp());
        event.setCreatedAt(Instant.now());
        return Optional.of(repository.save(event).getId());
    }
}
