package com.github.akarazhev.cryptoscout.collector;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

final class Converter {
    private Converter() {
        throw new UnsupportedOperationException();
    }

    public static OffsetDateTime toOffsetDateTime(final Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    public static OffsetDateTime toOffsetDateTimeFromSeconds(final long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    public static BigDecimal toBigDecimal(final Object value) {
        return switch (value) {
            case BigDecimal bd -> bd;
            case String s -> s.isEmpty() ? null : new BigDecimal(s);
            case Number n -> new BigDecimal(n.toString());
            case null, default -> null;
        };
    }
}
