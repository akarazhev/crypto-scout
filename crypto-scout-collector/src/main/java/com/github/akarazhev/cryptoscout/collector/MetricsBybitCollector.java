package com.github.akarazhev.cryptoscout.collector;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import com.github.akarazhev.cryptoscout.config.JdbcConfig;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_DESC;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_INSERT;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_RETURN_COIN;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_RETURN_COIN_ICON;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_RULES;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_STAKE_BEGIN_TIME;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_STAKE_END_TIME;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_TRADE_BEGIN_TIME;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_WEBSITE;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.LPL_WHITE_PAPER;
import static com.github.akarazhev.cryptoscout.collector.Utils.toOffsetDateTime;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DESC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN_ICON;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RULES;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_END_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TRADE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WEBSITE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WHITE_PAPER;

public final class MetricsBybitCollector extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsBybitCollector.class);
    private final Executor executor;
    private final DataSource dataSource;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Queue<Payload<Map<String, Object>>> buffer = new ConcurrentLinkedQueue<>();

    public static MetricsBybitCollector create(final NioReactor reactor, final Executor executor) {
        return new MetricsBybitCollector(reactor, executor);
    }

    private MetricsBybitCollector(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
        this.dataSource = JdbcConfig.getDataSource();
        this.batchSize = JdbcConfig.getBybitBatchSize();
        this.flushIntervalMs = JdbcConfig.getBybitFlushIntervalMs();
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting MetricsBybitCollector...");
        reactor.delayBackground(flushIntervalMs, this::scheduledFlush);
        LOGGER.info("MetricsBybitCollector started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping MetricsBybitCollector...");
        final var promise = flush();
        LOGGER.info("MetricsBybitCollector stopped");
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
            final var lpl = new ArrayList<Map<String, Object>>();
            for (final var payload : snapshot) {
                final var source = payload.getSource();
                if (Source.LPL.equals(source)) {
                    lpl.add(payload.getData());
                }
            }

            if (!lpl.isEmpty()) {
                insertLpl(lpl);
                LOGGER.info("Inserted {} LPL points", lpl.size());
            }

            return null;
        });
    }

    private void insertLpl(final Iterable<Map<String, Object>> lpls) throws Exception {
        try (final var c = dataSource.getConnection();
             final var ps = c.prepareStatement(LPL_INSERT)) {
            var count = 0;
            for (final var lpl : lpls) {
                ps.setString(LPL_RETURN_COIN, (String) lpl.get(RETURN_COIN));
                ps.setString(LPL_RETURN_COIN_ICON, (String) lpl.get(RETURN_COIN_ICON));
                ps.setString(LPL_DESC, (String) lpl.get(DESC));
                ps.setString(LPL_WEBSITE, (String) lpl.get(WEBSITE));
                ps.setString(LPL_WHITE_PAPER, (String) lpl.get(WHITE_PAPER));
                ps.setString(LPL_RULES, (String) lpl.get(RULES));
                ps.setObject(LPL_STAKE_BEGIN_TIME, toOffsetDateTime((Long) lpl.get(STAKE_BEGIN_TIME)));
                ps.setObject(LPL_STAKE_END_TIME, toOffsetDateTime((Long) lpl.get(STAKE_END_TIME)));
                final var tradeBegin = (Long) lpl.get(TRADE_BEGIN_TIME);
                if (tradeBegin != null) {
                    ps.setObject(LPL_TRADE_BEGIN_TIME, toOffsetDateTime(tradeBegin));
                } else {
                    ps.setNull(LPL_TRADE_BEGIN_TIME, Types.TIMESTAMP_WITH_TIMEZONE);
                }

                ps.addBatch();
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        }
    }
}
