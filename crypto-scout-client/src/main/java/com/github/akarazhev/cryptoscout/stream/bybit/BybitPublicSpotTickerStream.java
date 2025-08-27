package com.github.akarazhev.cryptoscout.stream.bybit;

import com.github.akarazhev.cryptoscout.stream.DataStream;
import com.github.akarazhev.jcryptolib.stream.Payload;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Qualifier("BybitPublicSpotTickerStream")
final class BybitPublicSpotTickerStream implements DataStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitPublicSpotTickerStream.class);
    private final BybitDataSupplier bybitDataSupplier;

    public BybitPublicSpotTickerStream(final BybitDataSupplier bybitDataSupplier) {
        this.bybitDataSupplier = bybitDataSupplier;
    }

    @Override
    public Flowable<Payload<Map<String, Object>>> stream() {
        return bybitDataSupplier.publicSpotTickerData()
                .subscribeOn(Schedulers.io())
                .doOnError((error) -> LOGGER.error("Bybit data stream error", error))
                .doOnCancel(() -> LOGGER.info("Bybit data stream cancelled"));
    }
}
