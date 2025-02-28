package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Event;

public interface EventPublisher {

    void publish(final Event event);
}
