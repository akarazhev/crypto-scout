package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Event;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
final class EventPublisherService implements EventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final String routing;

    public EventPublisherService(final AmqpTemplate amqpTemplate, @Value("${amqp.exchange.announcements}") final String exchange,
                                 @Value("${amqp.routing.announcements}") final String routing) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.routing = routing;
    }

    @Override
    public void publish(final Event event) {
        amqpTemplate.convertAndSend(exchange, routing, event);
    }
}
