package com.github.akarazhev.cryptoscout;

import java.util.stream.Stream;

public interface EventSource {

    Stream<Event> getEvents();
}
