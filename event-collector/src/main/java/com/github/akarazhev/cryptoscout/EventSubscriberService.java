package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.BybitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
final class EventSubscriberService implements EventSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriberService.class);
    private final BybitService bybitService;

    public EventSubscriberService(final BybitService bybitService) {
        this.bybitService = bybitService;
    }

    @RabbitListener(queues = "${amqp.queue.announcements}")
    @Override
    public void subscribe(final Event event) {
        if (Event.Platform.BYBIT.equals(event.platform())) {
            bybitService.save(event.eventType(), event.announcement());
        }
    }
}
