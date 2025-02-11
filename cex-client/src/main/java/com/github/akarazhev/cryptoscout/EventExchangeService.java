package com.github.akarazhev.cryptoscout;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
final class EventExchangeService implements EventExchange {
    private final AmqpTemplate amqpTemplate;
    private final String name;

    public EventExchangeService(final AmqpTemplate amqpTemplate, @Value("${amqp.exchange.announcements}") final String name) {
        this.amqpTemplate = amqpTemplate;
        this.name = name;
    }

    @Override
    public void publish(final Event event) {
        amqpTemplate.convertAndSend(name, "announcement." + event.platform() + "." + event.type(), event);
    }
}
