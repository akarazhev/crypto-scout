package com.github.akarazhev.cryptoscout.stream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public final class DataStreams {
    private final DataStream cmcDataStream;
    private final DataStream bybitEventStream;
    private final DataStream bybitPublicSpotTradeStream;

    public enum Type {
        CMC_DATA_STREAM,
        BYBIT_EVENT_STREAM,
        BYBIT_PUBLIC_SPOT_TRADE_STREAM;
    }

    public DataStreams(@Qualifier("cmcDataStream") final DataStream cmcDataStream,
                       @Qualifier("bybitEventStream") final DataStream bybitEventStream,
                       @Qualifier("bybitPublicSpotTradeDataStream") final DataStream bybitPublicSpotTradeStream) {
        this.cmcDataStream = cmcDataStream;
        this.bybitEventStream = bybitEventStream;
        this.bybitPublicSpotTradeStream = bybitPublicSpotTradeStream;
    }

    public DataStream of(final Type type) {
        return switch (type) {
            case CMC_DATA_STREAM -> cmcDataStream;
            case BYBIT_EVENT_STREAM -> bybitEventStream;
            case BYBIT_PUBLIC_SPOT_TRADE_STREAM -> bybitPublicSpotTradeStream;
        };
    }
}
