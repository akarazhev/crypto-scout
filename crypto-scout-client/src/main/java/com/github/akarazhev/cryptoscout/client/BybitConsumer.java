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

package com.github.akarazhev.cryptoscout.client;

import com.github.akarazhev.jcryptolib.bybit.stream.BybitParser;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitStream;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BybitConsumer extends AbstractReactive implements ReactiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitConsumer.class);
    private final BybitStream linearBybitStream;
    private final BybitStream spotBybitStream;
    private final BybitParser bybitParser;
    private final AmqpPublisher amqpPublisher;

    public static BybitConsumer create(final NioReactor reactor, final BybitStream linearBybitStream,
                                       final BybitStream spotBybitStream, final BybitParser bybitParser,
                                       final AmqpPublisher amqpPublisher) {
        return new BybitConsumer(reactor, linearBybitStream, spotBybitStream, bybitParser, amqpPublisher);
    }

    private BybitConsumer(final NioReactor reactor, final BybitStream linearBybitStream, final BybitStream spotBybitStream,
                          final BybitParser bybitParser, final AmqpPublisher amqpPublisher) {
        super(reactor);
        this.linearBybitStream = linearBybitStream;
        this.spotBybitStream = spotBybitStream;
        this.bybitParser = bybitParser;
        this.amqpPublisher = amqpPublisher;
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting BybitConsumer...");
        linearBybitStream.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
        spotBybitStream.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
        bybitParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
        LOGGER.info("BybitConsumer started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping BybitConsumer...");
        bybitParser.stop();
        spotBybitStream.stop();
        linearBybitStream.stop();
        LOGGER.info("BybitConsumer stopped");
        return Promise.complete();
    }
}
