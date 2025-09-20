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

package com.github.akarazhev.cryptoscout.stream;

import com.github.akarazhev.jcryptolib.bybit.stream.BybitParser;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitStream;
import com.github.akarazhev.jcryptolib.cmc.stream.CmcParser;
import io.activej.async.service.ReactiveService;
import io.activej.datastream.consumer.StreamConsumers;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StreamConsumer extends AbstractReactive implements ReactiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConsumer.class);
    private final BybitStream bybitStream;
    private final BybitParser bybitParser;
    private final CmcParser cmcParser;

    public static StreamConsumer create(final NioReactor reactor, final BybitStream bybitStream,
                                        final BybitParser bybitParser, final CmcParser cmcParser) {
        return new StreamConsumer(reactor, bybitStream, bybitParser, cmcParser);
    }

    private StreamConsumer(final NioReactor reactor, final BybitStream bybitStream, final BybitParser bybitParser,
                           final CmcParser cmcParser) {
        super(reactor);
        this.bybitStream = bybitStream;
        this.bybitParser = bybitParser;
        this.cmcParser = cmcParser;
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting the service...");
        bybitStream.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(payload ->
                        LOGGER.info("Bybit Stream: {}", payload.getData()))));
        bybitParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(payload ->
                        LOGGER.info("Bybit Parser: {}", payload.getData()))));
        cmcParser.start().then(stream ->
                stream.streamTo(StreamConsumers.ofConsumer(payload ->
                        LOGGER.info("CMC Parser: {}", payload.getData()))));
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping the service...");
        cmcParser.stop();
        bybitParser.stop();
        bybitStream.stop();
        return Promise.complete();
    }
}
