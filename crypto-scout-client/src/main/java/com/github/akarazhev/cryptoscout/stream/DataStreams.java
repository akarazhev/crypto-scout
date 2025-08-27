package com.github.akarazhev.cryptoscout.stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public final class DataStreams {
    private final DataStream cmcDataStream;
    private final DataStream bybitEventStream;
    private final DataStream bybitPublicSpotTickerStream;

    public enum Type {
        CMC_DATA_STREAM,
        BYBIT_EVENT_STREAM,
        BYBIT_PUBLIC_SPOT_TICKER_STREAM;
    }

    public DataStreams(@Qualifier("cmcDataStream") final DataStream cmcDataStream,
                       @Qualifier("bybitEventStream") final DataStream bybitEventStream,
                       @Qualifier("BybitPublicSpotTickerStream") final DataStream bybitPublicSpotTickerStream) {
        this.cmcDataStream = cmcDataStream;
        this.bybitEventStream = bybitEventStream;
        this.bybitPublicSpotTickerStream = bybitPublicSpotTickerStream;
    }

    public DataStream of(final Type type) {
        return switch (type) {
            case CMC_DATA_STREAM -> cmcDataStream;
            case BYBIT_EVENT_STREAM -> bybitEventStream;
            case BYBIT_PUBLIC_SPOT_TICKER_STREAM -> bybitPublicSpotTickerStream;
        };
    }
}
