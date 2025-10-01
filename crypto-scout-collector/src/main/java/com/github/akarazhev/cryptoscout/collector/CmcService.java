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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_BTC_PRICE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_BTC_VOLUME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_NAME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_SCORE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_TIMESTAMP;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_PRICE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_VOLUME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.DATA_LIST;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.NAME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.SCORE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIMESTAMP;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.CMC_FGI_INSERT;

public final class CmcService extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CmcService.class);
    private final Executor executor;
    private final DataSource dataSource;

    public static CmcService create(final NioReactor reactor, final Executor executor) {
        return new CmcService(reactor, executor);
    }

    private CmcService(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
        this.dataSource = JdbcConfig.getDataSource();
    }

    @Override
    public Promise<?> start() {
        return Promise.complete();
    }

    @Override
    public Promise<?> stop() {
        return Promise.complete();
    }

    public Promise<?> save(final Payload<Map<String, Object>> payload) {
        if (!Provider.CMC.equals(payload.getProvider())) {
            LOGGER.info("Invalid payload: {}", payload);
            return Promise.complete();
        }

        final var list = (java.util.List<Map<String, Object>>) payload.getData().get(DATA_LIST);
        if (list == null || list.isEmpty()) {
            LOGGER.info("No data to insert");
            return Promise.complete();
        }

        final var batchSize = JdbcConfig.getCmcBatchSize();
        return Promise.ofBlocking(executor, () -> {
            if (Source.FGI.equals(payload.getSource())) {
                insertFgi(list, batchSize);
            }

            return Promise.complete();
        });
    }

    private void insertFgi(final List<Map<String, Object>> points, final int batchSize) throws Exception {
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(CMC_FGI_INSERT)) {
            int count = 0;
            for (final var point : points) {
                // score
                final var score = point.get(SCORE);
                if (score instanceof Number n) {
                    ps.setInt(CMC_FGI_SCORE, n.intValue());
                } else if (score instanceof String s) {
                    ps.setInt(CMC_FGI_SCORE, Integer.parseInt(s));
                } else {
                    ps.setNull(CMC_FGI_SCORE, Types.INTEGER);
                }
                // name
                ps.setString(CMC_FGI_NAME, (String) point.get(NAME));
                // timestamp (seconds)
                final var ts = (String) point.get(TIMESTAMP);
                ps.setObject(CMC_FGI_TIMESTAMP, toOffsetDateTimeFromSeconds(ts != null ? Long.parseLong(ts) : 0L));
                // btc_price, btc_volume
                ps.setBigDecimal(CMC_FGI_BTC_PRICE, toBigDecimal(point.get(BTC_PRICE)));
                ps.setBigDecimal(CMC_FGI_BTC_VOLUME, toBigDecimal(point.get(BTC_VOLUME)));
                // add to batch
                ps.addBatch();
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        }
    }

    private static OffsetDateTime toOffsetDateTimeFromSeconds(final long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private static BigDecimal toBigDecimal(final Object value) {
        return switch (value) {
            case BigDecimal bd -> bd;
            case String s -> s.isEmpty() ? null : new BigDecimal(s);
            case Number n -> new BigDecimal(n.toString());
            case null, default -> null;
        };
    }
}
