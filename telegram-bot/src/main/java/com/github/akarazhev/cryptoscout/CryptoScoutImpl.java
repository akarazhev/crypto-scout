/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
            message.data().forEach(event -> response.add(event.toString()));
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
