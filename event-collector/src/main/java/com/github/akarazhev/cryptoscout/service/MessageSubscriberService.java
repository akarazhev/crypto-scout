package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Message;
import com.github.akarazhev.cryptoscout.bybit.BybitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.github.akarazhev.cryptoscout.Constants.AMQP_ROUTING_MESSAGES_RESULT;

@Service
final class MessageSubscriberService implements MessageSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSubscriberService.class);
    private final BybitService bybitService;
    private final AmqpTemplate amqpTemplate;
    private final String exchange;

    public MessageSubscriberService(final BybitService bybitService, final AmqpTemplate amqpTemplate,
                                    @Value("${amqp.exchange.messages}") final String exchange) {
        this.bybitService = bybitService;
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @RabbitListener(queues = "${amqp.queue.messages}")
    @Override
    public void subscribe(final Message message) {
        final var type = Message.Action.LAUNCH_POOL.equals(message.action()) ? "PoolLaunch" :
                Message.Action.LAUNCH_PAD.equals(message.action()) ? "Launchpad" : null;
        final var eventTime = (Long) message.data()[0];
        if (type != null && eventTime != null) {
            final var events = bybitService.getEvents(type, eventTime);
            final var data = new Object[events.size()];
            System.arraycopy(events.toArray(), 0, data, 0, events.size());
            amqpTemplate.convertAndSend(exchange, AMQP_ROUTING_MESSAGES_RESULT, new Message(message.chatId(), message.action(), data));
        } else {
            LOGGER.warn("Invalid message: {}", message);
        }
    }
}
