package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Event;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
final class EventPublisherService implements EventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final String name;

    public EventPublisherService(final AmqpTemplate amqpTemplate, @Value("${amqp.exchange.announcements}") final String name) {
        this.amqpTemplate = amqpTemplate;
        this.name = name;
    }

    @Override
    public void publish(final Event event) {
        amqpTemplate.convertAndSend(name, "announcement." + event.platform(), event);
    }
}
