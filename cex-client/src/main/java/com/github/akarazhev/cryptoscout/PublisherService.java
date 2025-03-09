package com.github.akarazhev.cryptoscout;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.github.akarazhev.cryptoscout.Constants.AMQP_ROUTING_ANNOUNCEMENTS;

@Service
final class PublisherService implements Publisher<Event> {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;

    public PublisherService(final AmqpTemplate amqpTemplate, @Value("${amqp.exchange.announcements}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @Override
    public void publish(final Event event) {
        amqpTemplate.convertAndSend(exchange, AMQP_ROUTING_ANNOUNCEMENTS, event);
    }
}
