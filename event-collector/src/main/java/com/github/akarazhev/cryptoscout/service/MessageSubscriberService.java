package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Message;
import com.github.akarazhev.cryptoscout.bybit.BybitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
final class MessageSubscriberService implements MessageSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSubscriberService.class);
    private final BybitService bybitService;
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final String routing;

    public MessageSubscriberService(final BybitService bybitService, final AmqpTemplate amqpTemplate,
                                    @Value("${amqp.exchange.messages}") final String exchange,
                                    @Value("${amqp.routing.messages}") final String routing) {
        this.bybitService = bybitService;
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.routing = routing;
    }

    @RabbitListener(queues = "${amqp.queue.messages}")
    @Override
    public void subscribe(final Message message) {
        final var type = Message.Action.LAUNCH_POOL.equals(message.action()) ? "PoolLaunch" :
                Message.Action.LAUNCH_PAD.equals(message.action()) ? "Launchpad" : null;
        final var eventTime = (Long) message.data()[0];
        if (type != null && eventTime != null) {
            bybitService.getEvents(type, eventTime).forEach(event -> {
                final var data = new Message(message.chatId(), message.action(), new Object[]{event});
                amqpTemplate.convertAndSend(exchange, routing, data);
            });
        } else {
            LOGGER.warn("Invalid message: {}", message);
        }
    }
}
