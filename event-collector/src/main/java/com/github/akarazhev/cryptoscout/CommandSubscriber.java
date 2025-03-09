package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.BybitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_RESULTS;

@Service
final class CommandSubscriber implements Subscriber<Message<Long>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandSubscriber.class);
    private final BybitService bybitService;
    private final AmqpTemplate amqpTemplate;
    private final String exchange;

    public CommandSubscriber(final BybitService bybitService, final AmqpTemplate amqpTemplate,
                             @Value("${amqp.exchange.results}") final String exchange) {
        this.bybitService = bybitService;
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @RabbitListener(queues = "${amqp.queue.commands}")
    @Override
    public void subscribe(final Message<Long> message) {
        final var action = message.action();
        final var type = Message.Action.LAUNCH_POOL.equals(action) ? "Launchpool" :
                Message.Action.LAUNCH_PAD.equals(action) ? "Launchpad" : null;
        if (type != null && message.data() != null) {
            final var events = bybitService.getEvents(type, message.data());
            amqpTemplate.convertAndSend(exchange, ROUTING_RESULTS, new Message<>(message.chatId(), action, events));
        } else {
            LOGGER.warn("Invalid message: {}", message);
        }
    }
}
