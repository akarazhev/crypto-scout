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
final class CommandSubscriber implements Subscriber<Message> {
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
    public void subscribe(final Message message) {
        final var type = Message.Action.LAUNCH_POOL.equals(message.action()) ? "Launchpool" :
                Message.Action.LAUNCH_PAD.equals(message.action()) ? "Launchpad" : null;
        if (type != null && message.data().length > 0) {
            final var events = bybitService.getEvents(type, (Long) message.data()[0]);
            final var data = new Object[events.size()];
            System.arraycopy(events.toArray(), 0, data, 0, events.size());
            amqpTemplate.convertAndSend(exchange, ROUTING_RESULTS, new Message(message.chatId(), message.action(), data));
        } else {
            LOGGER.warn("Invalid message: {}", message);
        }
    }
}
