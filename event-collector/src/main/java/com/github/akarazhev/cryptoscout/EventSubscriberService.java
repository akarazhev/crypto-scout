package com.github.akarazhev.cryptoscout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
final class EventSubscriberService implements EventSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriberService.class);

    @RabbitListener(queues = "${amqp.queue.announcements}")
    @Override
    public void subscribe(final Event event) {
        LOGGER.info(event.toString());
    }
}
