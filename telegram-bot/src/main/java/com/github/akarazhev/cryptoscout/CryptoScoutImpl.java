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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_COMMANDS;

@Service
final class CryptoScoutImpl implements CryptoScout {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final Cache<MessageKey, Collection<String>> results;
    private final Map<MessageKey, CompletableFuture<Collection<String>>> futures = new ConcurrentHashMap<>();

    public CryptoScoutImpl(final AmqpTemplate amqpTemplate,
                           @Value("${amqp.exchange.commands}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.results = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .evictionListener((key, value, cause) -> {
                    if (cause.wasEvicted()) {
                        CompletableFuture<Collection<String>> future = futures.remove(key);
                        if (future != null && !future.isDone()) {
                            future.completeExceptionally(new TimeoutException("Request timed out after 30 seconds"));
                        }
                    }
                })
                .build();
    }

    @Override
    public CompletableFuture<Collection<String>> getLaunchPads(long chatId, int days) {
        final CompletableFuture<Collection<String>> future = new CompletableFuture<>();
        futures.put(new MessageKey(chatId, Message.Action.LAUNCH_PAD), future);
        final var startDate = Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli();
        amqpTemplate.convertAndSend(exchange, ROUTING_COMMANDS,
                new Message<>(chatId, Message.Action.LAUNCH_PAD, startDate));
        return future;
    }

    @Override
    public CompletableFuture<Collection<String>> getLaunchPools(long chatId, int days) {
        final CompletableFuture<Collection<String>> future = new CompletableFuture<>();
        futures.put(new MessageKey(chatId, Message.Action.LAUNCH_POOL), future);
        final var startDate = Instant.now().minus(days, ChronoUnit.DAYS).toEpochMilli();
        amqpTemplate.convertAndSend(exchange, ROUTING_COMMANDS,
                new Message<>(chatId, Message.Action.LAUNCH_POOL, startDate));
        return future;
    }

    @RabbitListener(queues = "${amqp.queue.results}")
    @Override
    public void subscribe(final Message<Envelope<Event>> message) {
        final var key = new MessageKey(message.chatId(), message.action());
        final var future = futures.get(key);
        if (future != null) {
            final var envelope = message.data();
            if (message.action() != null && envelope != null) {
                var data = results.getIfPresent(key);
                if (data == null) {
                    data = new LinkedList<>();
                }

                data.add(processData(message));
                results.put(key, data);

                if (envelope.current() == envelope.total()) {
                    future.complete(results.getIfPresent(key));
                    results.invalidate(key);
                    futures.remove(key);
                }
            } else {
                future.completeExceptionally(new IllegalArgumentException("Invalid or unsupported message received"));
                futures.remove(key);
                results.invalidate(key);
            }
        }
    }

    private String processData(final Message<Envelope<Event>> message) {
        final var envelope = message.data();
        final var response = new StringJoiner("\n\n");
        if (envelope.current() > 0) {
            response.add(envelope.data().toString());
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
