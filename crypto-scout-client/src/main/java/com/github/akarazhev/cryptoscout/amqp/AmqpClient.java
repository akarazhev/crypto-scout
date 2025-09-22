package com.github.akarazhev.cryptoscout.amqp;

import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public final class AmqpClient extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpClient.class);
    private final Executor executor;

    public static AmqpClient create(final NioReactor reactor, final Executor executor) {
        return new AmqpClient(reactor, executor);
    }

    private AmqpClient(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
            LOGGER.info("AmqpClient started");
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
            LOGGER.info("AmqpClient stopped");
        });
    }
}
