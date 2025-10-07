package com.github.akarazhev.cryptoscout.collector;

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

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_BTC_USDT;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_CROSS_SEQUENCE;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_ETH_USDT;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_HIGH_PRICE_24H;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_INSERT;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_LAST_PRICE;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_LOW_PRICE_24H;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_PREV_PRICE_24H;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_PRICE_24H_PCNT;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_TIMESTAMP;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_TURNOVER_24H;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_USD_INDEX_PRICE;
import static com.github.akarazhev.cryptoscout.collector.Constants.Bybit.SPOT_TICKERS_VOLUME_24H;
import static com.github.akarazhev.cryptoscout.collector.Utils.toBigDecimal;
import static com.github.akarazhev.cryptoscout.collector.Utils.toOffsetDateTime;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.CS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DATA;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.HIGH_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LAST_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LOW_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PREV_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PRICE_24H_PCNT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TURNOVER_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.USD_INDEX_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.VOLUME_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_BTC_USDT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_ETH_USDT;

public final class CryptoBybitCollector extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CryptoBybitCollector.class);
    private final Executor executor;
    private final DataSource dataSource;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Queue<Payload<Map<String, Object>>> buffer = new ConcurrentLinkedQueue<>();

    public static CryptoBybitCollector create(final NioReactor reactor, final Executor executor,
                                              final JdbcDataSource jdbcDataSource) {
        return new CryptoBybitCollector(reactor, executor, jdbcDataSource);
    }

    private CryptoBybitCollector(final NioReactor reactor, final Executor executor, final JdbcDataSource jdbcDataSource) {
        super(reactor);
        this.executor = executor;
        this.dataSource = jdbcDataSource.getDataSource();
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
                insertSpot(SPOT_TICKERS_BTC_USDT, spotTickersBtcUsdt);
                LOGGER.info("Inserted {} BTC-USDT spot tickers", snapshot.size());
            }

            if (!spotTickersEthUsdt.isEmpty()) {
                insertSpot(SPOT_TICKERS_ETH_USDT, spotTickersEthUsdt);
                LOGGER.info("Inserted {} ETH-USDT spot tickers", snapshot.size());
            }

            return null;
        });
    }

    private void insertSpot(final String table, final Iterable<Map<String, Object>> rows) throws Exception {
        try (final var c = dataSource.getConnection();
             final var ps = c.prepareStatement(String.format(SPOT_TICKERS_INSERT, table))) {
            int count = 0;
            for (final var row : rows) {
                final var dObj = row.get(DATA);
                if (!(dObj instanceof Map<?, ?> map)) {
                    // skip malformed rows
                    continue;
                }

                final var odt = toOffsetDateTime((Long) row.get(TS));
                if (odt != null) {
                    ps.setObject(SPOT_TICKERS_TIMESTAMP, odt);
                } else {
                    ps.setNull(SPOT_TICKERS_TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE);
                }

                final var cs = (Long) row.get(CS);
                if (cs != null) {
                    ps.setObject(SPOT_TICKERS_CROSS_SEQUENCE, cs);
                } else {
                    ps.setNull(SPOT_TICKERS_CROSS_SEQUENCE, Types.BIGINT);
                }

                @SuppressWarnings("unchecked") final var d = (Map<String, Object>) map;
                ps.setBigDecimal(SPOT_TICKERS_LAST_PRICE, toBigDecimal(d.get(LAST_PRICE)));
                ps.setBigDecimal(SPOT_TICKERS_HIGH_PRICE_24H, toBigDecimal(d.get(HIGH_PRICE_24H)));
                ps.setBigDecimal(SPOT_TICKERS_LOW_PRICE_24H, toBigDecimal(d.get(LOW_PRICE_24H)));
                ps.setBigDecimal(SPOT_TICKERS_PREV_PRICE_24H, toBigDecimal(d.get(PREV_PRICE_24H)));
                ps.setBigDecimal(SPOT_TICKERS_VOLUME_24H, toBigDecimal(d.get(VOLUME_24H)));
                ps.setBigDecimal(SPOT_TICKERS_TURNOVER_24H, toBigDecimal(d.get(TURNOVER_24H)));
                ps.setBigDecimal(SPOT_TICKERS_PRICE_24H_PCNT, toBigDecimal(d.get(PRICE_24H_PCNT)));
                // may be null
                final var usd = toBigDecimal(d.get(USD_INDEX_PRICE));
                if (usd != null) {
                    ps.setBigDecimal(SPOT_TICKERS_USD_INDEX_PRICE, usd);
                } else {
                    ps.setNull(SPOT_TICKERS_USD_INDEX_PRICE, Types.NUMERIC);
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
