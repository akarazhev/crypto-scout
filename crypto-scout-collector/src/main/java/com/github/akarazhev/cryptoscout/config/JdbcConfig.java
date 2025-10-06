package com.github.akarazhev.cryptoscout.config;

import com.github.akarazhev.jcryptolib.config.AppConfig;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_BYBIT_BATCH_SIZE;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_BYBIT_FLUSH_INTERVAL_MS;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_CMC_BATCH_SIZE;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_CMC_FLUSH_INTERVAL_MS;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_PASSWORD;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_URL;
import static com.github.akarazhev.cryptoscout.config.Constants.JdbcConfig.JDBC_USERNAME;

public final class JdbcConfig {
    private JdbcConfig() {
        throw new UnsupportedOperationException();
    }

    private static String getUrl() {
        return AppConfig.getAsString(JDBC_URL);
    }

    private static String getUsername() {
        return AppConfig.getAsString(JDBC_USERNAME);
    }

    private static String getPassword() {
        return AppConfig.getAsString(JDBC_PASSWORD);
    }

    public static int getCmcBatchSize() {
        return AppConfig.getAsInt(JDBC_CMC_BATCH_SIZE);
    }

    public static long getCmcFlushIntervalMs() {
        return AppConfig.getAsInt(JDBC_CMC_FLUSH_INTERVAL_MS);
    }

    public static int getBybitBatchSize() {
        return AppConfig.getAsInt(JDBC_BYBIT_BATCH_SIZE);
    }

    public static long getBybitFlushIntervalMs() {
        return AppConfig.getAsInt(JDBC_BYBIT_FLUSH_INTERVAL_MS);
    }

    public static DataSource getDataSource() {
        final var ds = new PGSimpleDataSource();
        ds.setURL(getUrl());
        ds.setUser(getUsername());
        ds.setPassword(getPassword());
        return ds;
    }
}
