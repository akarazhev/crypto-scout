package com.github.akarazhev.cryptoscout.collector;

import com.github.akarazhev.jcryptolib.stream.Payload;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executor;

public final class CmcService extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CmcService.class);
    private final Executor executor;

    public static CmcService create(final NioReactor reactor, final Executor executor) {
        return new CmcService(reactor, executor);
    }

    private CmcService(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
        });
    }

    public Promise<?> save(final Payload<Map<String, Object>> payload) {
        return Promise.ofBlocking(executor, () -> {
        });
    }
}
