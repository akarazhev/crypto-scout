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
