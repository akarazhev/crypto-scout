package com.github.akarazhev.cryptoscout;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_COMMANDS;

@Service
final class CryptoScoutImpl implements CryptoScout {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final Map<MessageKey, CompletableFuture<String>> pending;

    public CryptoScoutImpl(final AmqpTemplate amqpTemplate,
                           @Value("${amqp.exchange.commands}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.pending = new ConcurrentHashMap<>();
    }

    @Override
    public CompletableFuture<String> getLaunchPads(long chatId, int days) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(new MessageKey(chatId, Message.Action.LAUNCH_PAD), future);
        final var startDate = Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli();
        amqpTemplate.convertAndSend(exchange, ROUTING_COMMANDS,
                new Message<>(chatId, Message.Action.LAUNCH_PAD, startDate));
        return future;
    }

    @Override
    public CompletableFuture<String> getLaunchPools(long chatId, int days) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(new MessageKey(chatId, Message.Action.LAUNCH_POOL), future);
        final var startDate = Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli();
        amqpTemplate.convertAndSend(exchange, ROUTING_COMMANDS,
                new Message<>(chatId, Message.Action.LAUNCH_POOL, startDate));
        return future;
    }

    @RabbitListener(queues = "${amqp.queue.results}")
    @Override
    public void subscribe(final Message<List<Event>> message) {
        final var future = pending.remove(new MessageKey(message.chatId(), message.action()));
        if (future != null) {
            if (message.action() != null && message.data() != null) {
                future.complete(processData(message));
            } else {
                future.completeExceptionally(new IllegalArgumentException("Invalid or unsupported message received"));
            }
        }
    }

    private String processData(final Message<List<Event>> message) {
        final var response = new StringJoiner("\n\n");
        if (!message.data().isEmpty()) {
            for (int i = 0; i < message.data().size(); i++) {
                response.add(message.data().get(i).toString());
            }
        } else {
            response.add(Message.Action.LAUNCH_POOL.equals(message.action()) ? "No launch pools found" :
                    Message.Action.LAUNCH_PAD.equals(message.action()) ? "No launch pads found" :
                            "No results found");
        }

        return response.toString();
    }

    private record MessageKey(long chatId, Message.Action action) {
        @Override
        public boolean equals(final Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final var that = (MessageKey) o;
            return chatId == that.chatId && action == that.action;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chatId, action);
        }

        @Override
        public String toString() {
            return "MessageKey{" +
                    "chatId=" + chatId +
                    ", action=" + action +
                    '}';
        }
    }
}
