package com.github.akarazhev.cryptoscout;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
final class EventSubscriberService implements EventSubscriber {

    @RabbitListener(queues = "${amqp.queue.announcements}")
    @Override
    public void subscribe(final Event event) {
        System.out.println(event);
    }
}
