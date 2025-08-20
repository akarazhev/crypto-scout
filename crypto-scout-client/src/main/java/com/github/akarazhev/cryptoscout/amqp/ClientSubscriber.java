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

package com.github.akarazhev.cryptoscout.amqp;

import com.github.akarazhev.cryptoscout.Command;
import com.github.akarazhev.cryptoscout.Subscriber;
import com.github.akarazhev.cryptoscout.stream.DataBridge;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
final class ClientSubscriber implements Subscriber<Command<Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSubscriber.class);
    private final DataBridge dataBridge;

    public ClientSubscriber(final DataBridge dataBridge) {
        this.dataBridge = dataBridge;
    }

    @RabbitListener(queues = "${amqp.queue.client}")
    @Override
    public void subscribe(final Command<Object> command) {
        LOGGER.info("Received the command: {}", command);
        switch (command.action()) {
            case STOP -> dataBridge.stop();
            case START -> dataBridge.start();
            case RESTART -> dataBridge.restart();
            default -> {
            }
        }
    }

    @PreDestroy
    public void stop() {
        dataBridge.stop();
    }

    @PostConstruct
    public void start() {
        dataBridge.start();
    }
}
