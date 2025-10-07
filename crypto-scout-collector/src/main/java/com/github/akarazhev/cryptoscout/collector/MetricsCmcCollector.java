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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_BTC_PRICE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_BTC_VOLUME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_INSERT;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_NAME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_SCORE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_TIMESTAMP;
import static com.github.akarazhev.cryptoscout.collector.Utils.toBigDecimal;
import static com.github.akarazhev.cryptoscout.collector.Utils.toOffsetDateTimeFromSeconds;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_PRICE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_VOLUME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.DATA_LIST;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.NAME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.SCORE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIMESTAMP;

public final class MetricsCmcCollector extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsCmcCollector.class);
    private final Executor executor;
    private final DataSource dataSource;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Queue<Payload<Map<String, Object>>> buffer = new ConcurrentLinkedQueue<>();

    public static MetricsCmcCollector create(final NioReactor reactor, final Executor executor,
                                             final JdbcDataSource jdbcDataSource) {
        return new MetricsCmcCollector(reactor, executor, jdbcDataSource);
    }

    private MetricsCmcCollector(final NioReactor reactor, final Executor executor, final JdbcDataSource jdbcDataSource) {
        super(reactor);
        this.executor = executor;
        this.dataSource = jdbcDataSource.getDataSource();
        this.batchSize = JdbcConfig.getCmcBatchSize();
        this.flushIntervalMs = JdbcConfig.getCmcFlushIntervalMs();
    }

    @Override
    public Promise<?> start() {
        LOGGER.info("Starting MetricsCmcCollector...");
        reactor.delayBackground(flushIntervalMs, this::scheduledFlush);
        LOGGER.info("MetricsCmcCollector started");
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        LOGGER.info("Stopping MetricsCmcCollector...");
        final var promise = flush();
        LOGGER.info("MetricsCmcCollector stopped");
        return promise;
    }

    public Promise<?> save(final Payload<Map<String, Object>> payload) {
        if (!Provider.CMC.equals(payload.getProvider())) {
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
            final var fgi = new ArrayList<Map<String, Object>>();
            for (final var payload : snapshot) {
                final var source = payload.getSource();
                if (Source.FGI.equals(source)) {
                    fgi.add(payload.getData());
                }
            }

            if (!fgi.isEmpty()) {
                insertFgi(fgi);
                LOGGER.info("Inserted {} FGI points", fgi.size());
            }

            return null;
        });
    }

    private void insertFgi(final List<Map<String, Object>> fgis) throws Exception {
        try (final var c = dataSource.getConnection();
             final var ps = c.prepareStatement(FGI_INSERT)) {
            var count = 0;
            for (final var fgi : fgis) {
                if (fgi != null && fgi.containsKey(DATA_LIST)) {
                    for (final var dl : (List<Map<String, Object>>) fgi.get(DATA_LIST)) {
                        final var score = dl.get(SCORE);
                        if (score instanceof Number n) {
                            ps.setInt(FGI_SCORE, n.intValue());
                        } else if (score instanceof String s) {
                            ps.setInt(FGI_SCORE, Integer.parseInt(s));
                        } else {
                            ps.setNull(FGI_SCORE, Types.INTEGER);
                        }

                        ps.setString(FGI_NAME, (String) dl.get(NAME));
                        final var ts = (String) dl.get(TIMESTAMP);
                        ps.setObject(FGI_TIMESTAMP, toOffsetDateTimeFromSeconds(ts != null ? Long.parseLong(ts) : 0L));
                        ps.setBigDecimal(FGI_BTC_PRICE, toBigDecimal(dl.get(BTC_PRICE)));
                        ps.setBigDecimal(FGI_BTC_VOLUME, toBigDecimal(dl.get(BTC_VOLUME)));

                        ps.addBatch();
                        if (++count % batchSize == 0) {
                            ps.executeBatch();
                        }
                    }
                }
            }

            ps.executeBatch();
        }
    }
}
