package com.github.akarazhev.cryptoscout.service;

//import com.github.akarazhev.cryptoscout.Event;
import com.github.akarazhev.cryptoscout.Message;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.akarazhev.cryptoscout.Constants.AMQP_ROUTING_COMMANDS;

@Service
final class LaunchPoolService implements LaunchPool {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final Map<Long, CompletableFuture<String[]>> pendingRequests = new ConcurrentHashMap<>();

    public LaunchPoolService(final AmqpTemplate amqpTemplate,
                             @Value("${amqp.exchange.commands}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @Async
    @Override
    public CompletableFuture<String[]> getLaunchPools(final long chatId, final int days) {
        final var startDate = Instant.now().minus(100, ChronoUnit.DAYS).toEpochMilli();
        final CompletableFuture<String[]> future = new CompletableFuture<>();
        pendingRequests.put(chatId, future);
        amqpTemplate.convertAndSend(exchange, AMQP_ROUTING_COMMANDS,
                new Message(chatId, Message.Action.LAUNCH_POOL, new Object[]{startDate}));
        return future;
    }

    @RabbitListener(queues = "${amqp.queue.results}")
    @Override
    public void subscribe(final Message message) {
        final var future = pendingRequests.remove(message.chatId());
        if (future != null) {
            if (message.action() != null && message.data() != null && message.data().length > 0) {
                future.complete(processLaunchPoolData(message));
            } else {
                future.completeExceptionally(new IllegalArgumentException("Invalid or unsupported message received"));
            }
        }
    }

    private String[] processLaunchPoolData(final Message message) {
        final var response = new String[message.data().length];
        for (int i = 0; i < message.data().length; i++) {
//            final var event = (Event) message.data()[i];
//            response[i] = event.type() + " on " + event.platform() + ": \n" +
//                    "Published At: " + event.eventTime() + "\n" +
//                    "Title: " + event.title() + "\n" +
//                    "Description: " + event.description() + "\n" +
//                    "URL: " + event.url();
            response[i] = message.data()[i].toString();
        }

        return response;
    }
}