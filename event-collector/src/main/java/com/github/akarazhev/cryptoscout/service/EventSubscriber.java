package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Event;

public interface EventSubscriber {

    void subscribe(final Event event);
}
