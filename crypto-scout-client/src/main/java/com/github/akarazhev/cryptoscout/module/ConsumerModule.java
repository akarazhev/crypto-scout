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

package com.github.akarazhev.cryptoscout.module;

import com.github.akarazhev.cryptoscout.consumer.AmqpClient;
import com.github.akarazhev.cryptoscout.consumer.BybitConsumer;
import com.github.akarazhev.cryptoscout.consumer.CmcConsumer;
import com.github.akarazhev.cryptoscout.consumer.Publisher;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitParser;
import com.github.akarazhev.jcryptolib.bybit.stream.BybitStream;
import com.github.akarazhev.jcryptolib.cmc.stream.CmcParser;
import com.github.akarazhev.jcryptolib.stream.Payload;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Named;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.reactor.nio.NioReactor;

import java.util.Map;
import java.util.concurrent.Executor;

public final class ConsumerModule extends AbstractModule {

    private ConsumerModule() {
    }

    public static ConsumerModule create() {
        return new ConsumerModule();
    }

    @Provides
    private Publisher<Payload<Map<String, Object>>> publisher(final AmqpClient amqpClient) {
        return amqpClient;
    }

    @Provides
    @Eager
    private AmqpClient amqpClient(final NioReactor reactor, final Executor executor) {
        return AmqpClient.create(reactor, executor);
    }

    @Eager
    @Provides
    private BybitConsumer bybitConsumer(final NioReactor reactor,
                                        @Named("linearBybitStream") final BybitStream linearBybitStream,
                                        @Named("spotBybitStream") final BybitStream spotBybitStream,
                                        final BybitParser bybitParser,
                                        final Publisher<Payload<Map<String, Object>>> publisher) {
        return BybitConsumer.create(reactor, linearBybitStream, spotBybitStream, bybitParser, publisher);
    }

    @Eager
    @Provides
    private CmcConsumer cmcConsumer(final NioReactor reactor, final CmcParser cmcParser,
                                    final Publisher<Payload<Map<String, Object>>> publisher) {
        return CmcConsumer.create(reactor, cmcParser, publisher);
    }
}
