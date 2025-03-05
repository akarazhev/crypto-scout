package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.cryptoscout.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Service
class BybitServiceImpl implements BybitService {
    private final BybitEventRepository repository;

    public BybitServiceImpl(final BybitEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public Optional<Long> save(final Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (repository.existsByTitle(event.title())) {
            return Optional.empty();
        }

        final var bybitEvent = new BybitEvent();
        bybitEvent.setType(event.type());
        bybitEvent.setTitle(event.title());
        bybitEvent.setDescription(event.description());
        bybitEvent.setUrl(event.url());
        bybitEvent.setEventTime(event.eventTime());
        bybitEvent.setCreatedAt(Instant.now());
        return Optional.of(repository.save(bybitEvent).getId());
    }

    @Override
    public Collection<Event> getEvents(final long afterTimestamp) {
        return repository.findByEventTimeAfter(afterTimestamp)
                .stream()
                .map(e -> new Event(Event.Platform.BYBIT, e.getEventTime(), e.getType(), e.getTitle(),
                        e.getDescription(), e.getUrl()))
                .toList();
    }
}
