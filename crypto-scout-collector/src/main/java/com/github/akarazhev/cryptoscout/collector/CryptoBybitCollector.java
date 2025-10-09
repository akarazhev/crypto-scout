package com.github.akarazhev.cryptoscout.collector;

import com.github.akarazhev.cryptoscout.collector.db.CryptoBybitRepository;
import com.github.akarazhev.cryptoscout.config.JdbcConfig;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.db.Constants.Bybit.SPOT_TICKERS_BTC_USDT;
import static com.github.akarazhev.cryptoscout.collector.db.Constants.Bybit.SPOT_TICKERS_ETH_USDT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_BTC_USDT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_ETH_USDT;

public final class CryptoBybitCollector extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CryptoBybitCollector.class);
    private final Executor executor;
    private final CryptoBybitRepository cryptoBybitRepository;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Queue<Payload<Map<String, Object>>> buffer = new ConcurrentLinkedQueue<>();

    public static CryptoBybitCollector create(final NioReactor reactor, final Executor executor,
                                              final CryptoBybitRepository cryptoBybitRepository) {
        return new CryptoBybitCollector(reactor, executor, cryptoBybitRepository);
    }

    private CryptoBybitCollector(final NioReactor reactor, final Executor executor,
                                 final CryptoBybitRepository cryptoBybitRepository) {
        super(reactor);
        this.executor = executor;
        this.cryptoBybitRepository = cryptoBybitRepository;
        this.batchSize = JdbcConfig.getBybitBatchSize();
        this.flushIntervalMs = JdbcConfig.getBybitFlushIntervalMs();
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting CryptoBybitCollector...");
        reactor.delayBackground(flushIntervalMs, this::scheduledFlush);
        LOGGER.info("CryptoBybitCollector started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping CryptoBybitCollector...");
        final var promise = flush();
        LOGGER.info("CryptoBybitCollector stopped");
        return promise;
    }

    public Promise<?> save(final Payload<Map<String, Object>> payload) {
        if (!Provider.BYBIT.equals(payload.getProvider())) {
            LOGGER.info("Invalid payload: {}", payload);
            return Promise.complete();
        }

        buffer.add(payload);
        if (buffer.size() >= batchSize) {
            return flush();
        }

        return Promise.complete();
    }

    private void scheduledFlush() {
        flush().whenComplete(($, e) ->
                reactor.delayBackground(flushIntervalMs, this::scheduledFlush));
    }

    private Promise<?> flush() {
        if (buffer.isEmpty()) {
            return Promise.complete();
        }

        final var snapshot = new ArrayList<Payload<Map<String, Object>>>();
        while (true) {
            final var item = buffer.poll();
            if (item == null) {
                break;
            }

            snapshot.add(item);
        }

        if (snapshot.isEmpty()) {
            return Promise.complete();
        }

        return Promise.ofBlocking(executor, () -> {
            final var spotTickersBtcUsdt = new ArrayList<Map<String, Object>>();
            final var spotTickersEthUsdt = new ArrayList<Map<String, Object>>();
            for (final var payload : snapshot) {
                final var data = payload.getData();
                final var topic = (String) data.get(TOPIC);
                final var source = payload.getSource();
                if (Source.PMST.equals(source)) {
                    if (Objects.equals(topic, TICKERS_BTC_USDT)) {
                        spotTickersBtcUsdt.add(data);
                    } else if (Objects.equals(topic, TICKERS_ETH_USDT)) {
                        spotTickersEthUsdt.add(data);
                    }
                } else if (Source.PML.equals(source)) {
                    // TODO: implement futures
                }
            }

            if (!spotTickersBtcUsdt.isEmpty()) {
                LOGGER.info("Inserted {} BTC-USDT spot tickers",
                        cryptoBybitRepository.insertSpot(SPOT_TICKERS_BTC_USDT, spotTickersBtcUsdt));
            }

            if (!spotTickersEthUsdt.isEmpty()) {
                LOGGER.info("Inserted {} ETH-USDT spot tickers",
                        cryptoBybitRepository.insertSpot(SPOT_TICKERS_ETH_USDT, spotTickersEthUsdt));
            }

            return null;
        });
    }
}
