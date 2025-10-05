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

import com.github.akarazhev.jcryptolib.cmc.stream.CmcParser;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CmcParserConsumer extends AbstractReactive implements ReactiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmcParserConsumer.class);
    private final CmcParser cmcParser;
    private final AmqpPublisher amqpPublisher;

    public static CmcParserConsumer create(final NioReactor reactor, final CmcParser cmcParser,
                                           final AmqpPublisher amqpPublisher) {
        return new CmcParserConsumer(reactor, cmcParser, amqpPublisher);
    }

    private CmcParserConsumer(final NioReactor reactor, final CmcParser cmcParser,
                              final AmqpPublisher amqpPublisher) {
        super(reactor);
        this.cmcParser = cmcParser;
        this.amqpPublisher = amqpPublisher;
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting CmcParserConsumer...");
        cmcParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(amqpPublisher::publish)));
        LOGGER.info("CmcParserConsumer started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping CmcParserConsumer...");
        cmcParser.stop();
        LOGGER.info("CmcParserConsumer stopped");
        return Promise.complete();
    }
}
