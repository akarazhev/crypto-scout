package com.github.akarazhev.cryptoscout.collector;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import com.github.akarazhev.cryptoscout.config.JdbcConfig;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.promise.Promises;
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
import static com.github.akarazhev.cryptoscout.collector.Converter.toBigDecimal;
import static com.github.akarazhev.cryptoscout.collector.Converter.toOffsetDateTime;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.CS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DATA;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.DESC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.HIGH_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LAST_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.LOW_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PREV_PRICE_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.PRICE_24H_PCNT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RETURN_COIN_ICON;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.RULES;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.STAKE_END_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TOPIC;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TRADE_BEGIN_TIME;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TS;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.TURNOVER_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.USD_INDEX_PRICE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.VOLUME_24H;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WEBSITE;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Response.WHITE_PAPER;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_BTC_USDT;
import static com.github.akarazhev.jcryptolib.bybit.Constants.Topic.TICKERS_ETH_USDT;

public final class BybitService extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(BybitService.class);
    private final Executor executor;
    private final DataSource dataSource;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Queue<Map<String, Object>> tickersBuffer = new ConcurrentLinkedQueue<>();
    private final Queue<Map<String, Object>> lplBuffer = new ConcurrentLinkedQueue<>();

    public static BybitService create(final NioReactor reactor, final Executor executor) {
        return new BybitService(reactor, executor);
    }

    private BybitService(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
        this.dataSource = JdbcConfig.getDataSource();
        this.batchSize = JdbcConfig.getBybitBatchSize();
        this.flushIntervalMs = JdbcConfig.getBybitFlushIntervalMs();
    }

    @Override
    public Promise<?> start() {
        reactor.delayBackground(flushIntervalMs, this::scheduledFlush);
        LOGGER.info("Started Bybit service");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Flushing all buffers");
        return flushAll();
    }

    public Promise<?> save(final Payload<Map<String, Object>> payload) {
        if (!Provider.BYBIT.equals(payload.getProvider())) {
            return Promise.complete();
        }

        final var source = payload.getSource();
        if (Source.LPL.equals(source)) {
            lplBuffer.add(payload.getData());
            if (lplBuffer.size() >= batchSize) {
                return flushLpl();
            }
        } else if (Source.PMST.equals(source)) {
            final var data = payload.getData();
            final var topic = (String) data.get(TOPIC);
            if (Objects.equals(topic, TICKERS_BTC_USDT) || Objects.equals(topic, TICKERS_ETH_USDT)) {
                tickersBuffer.add(data);
                if (tickersBuffer.size() >= batchSize) {
                    return flushTickers();
                }
            }
        }

        return Promise.complete();
    }

    private void scheduledFlush() {
        flushAll().whenComplete(($, e) ->
                reactor.delayBackground(flushIntervalMs, this::scheduledFlush));
    }

    private Promise<?> flushAll() {
        return Promises.all(flushTickers(), flushLpl());
    }

    private Promise<?> flushTickers() {
        if (tickersBuffer.isEmpty()) {
            return Promise.complete();
        }

        final var snapshot = new ArrayList<Map<String, Object>>();
        while (true) {
            final var item = tickersBuffer.poll();
            if (item == null) {
                break;
            }

            snapshot.add(item);
        }

        if (snapshot.isEmpty()) {
            return Promise.complete();
        }

        return Promise.ofBlocking(executor, () -> {
            final var btc = new ArrayList<Map<String, Object>>();
            final var eth = new ArrayList<Map<String, Object>>();
            for (final var m : snapshot) {
                final var topic = (String) m.get(TOPIC);
                if (Objects.equals(topic, TICKERS_BTC_USDT)) {
                    btc.add(m);
                } else if (Objects.equals(topic, TICKERS_ETH_USDT)) {
                    eth.add(m);
                }
            }

            if (!btc.isEmpty()) {
                insertSpot(SPOT_TICKERS_BTC_USDT, btc);
                LOGGER.info("Inserted {} BTC-USDT spot tickers", snapshot.size());
            }

            if (!eth.isEmpty()) {
                insertSpot(SPOT_TICKERS_ETH_USDT, eth);
                LOGGER.info("Inserted {} ETH-USDT spot tickers", snapshot.size());
            }

            return null;
        });
    }

    private Promise<?> flushLpl() {
        if (lplBuffer.isEmpty()) {
            return Promise.complete();
        }

        final var snapshot = new ArrayList<Map<String, Object>>();
        while (true) {
            final var item = lplBuffer.poll();
            if (item == null) {
                break;
            }

            snapshot.add(item);
        }

        if (snapshot.isEmpty()) {
            return Promise.complete();
        }

        return Promise.ofBlocking(executor, () -> {
            insertLpl(snapshot);
            LOGGER.info("Inserted {} LPL points", snapshot.size());
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

    private void insertLpl(final Iterable<Map<String, Object>> rows) throws Exception {
        try (final var c = dataSource.getConnection();
             final var ps = c.prepareStatement(LPL_INSERT)) {
            var count = 0;
            for (final var row : rows) {
                ps.setString(LPL_RETURN_COIN, (String) row.get(RETURN_COIN));
                ps.setString(LPL_RETURN_COIN_ICON, (String) row.get(RETURN_COIN_ICON));
                ps.setString(LPL_DESC, (String) row.get(DESC));
                ps.setString(LPL_WEBSITE, (String) row.get(WEBSITE));
                ps.setString(LPL_WHITE_PAPER, (String) row.get(WHITE_PAPER));
                ps.setString(LPL_RULES, (String) row.get(RULES));
                ps.setObject(LPL_STAKE_BEGIN_TIME, toOffsetDateTime((Long) row.get(STAKE_BEGIN_TIME)));
                ps.setObject(LPL_STAKE_END_TIME, toOffsetDateTime((Long) row.get(STAKE_END_TIME)));
                final var tradeBegin = (Long) row.get(TRADE_BEGIN_TIME);
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
