package com.github.akarazhev.cryptoscout.bybit;

import java.util.Optional;

public interface BybitService {

    Optional<Long> save(final String eventType, final Announcement announcement);
}
