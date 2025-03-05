package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.cryptoscout.Event;

import java.util.List;
import java.util.Optional;

public interface BybitService {

    Optional<Long> save(final Event event);

    List<Event> getEvents(final String type, final long eventTime);
}
