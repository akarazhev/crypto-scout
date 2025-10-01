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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_BTC_PRICE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_BTC_VOLUME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_INSERT;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_NAME;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_SCORE;
import static com.github.akarazhev.cryptoscout.collector.Constants.CMC.FGI_TIMESTAMP;
import static com.github.akarazhev.cryptoscout.collector.Converter.toBigDecimal;
import static com.github.akarazhev.cryptoscout.collector.Converter.toOffsetDateTimeFromSeconds;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_PRICE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.BTC_VOLUME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.DATA_LIST;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.NAME;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.SCORE;
import static com.github.akarazhev.jcryptolib.cmc.Constants.Response.TIMESTAMP;

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
        try (final var con = dataSource.getConnection(); final var ps = con.prepareStatement(FGI_INSERT)) {
            var count = 0;
            for (final var point : points) {
                final var score = point.get(SCORE);
                if (score instanceof Number n) {
                    ps.setInt(FGI_SCORE, n.intValue());
                } else if (score instanceof String s) {
                    ps.setInt(FGI_SCORE, Integer.parseInt(s));
                } else {
                    ps.setNull(FGI_SCORE, Types.INTEGER);
                }

                ps.setString(FGI_NAME, (String) point.get(NAME));
                final var ts = (String) point.get(TIMESTAMP);
                ps.setObject(FGI_TIMESTAMP, toOffsetDateTimeFromSeconds(ts != null ? Long.parseLong(ts) : 0L));
                ps.setBigDecimal(FGI_BTC_PRICE, toBigDecimal(point.get(BTC_PRICE)));
                ps.setBigDecimal(FGI_BTC_VOLUME, toBigDecimal(point.get(BTC_VOLUME)));

                ps.addBatch();
                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        }
    }
}
