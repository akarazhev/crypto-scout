package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Event;

interface EventSubscriber {

    void subscribe(final Event event);
}
