/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.akarazhev.cryptoscout.config;

final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class AmqpConfig {
        private AmqpConfig() {
            throw new UnsupportedOperationException();
        }

        static final String CONNECTION_NAME = "crypto-scout-collector";
        static final String AMQP_RABBITMQ_HOST = "amqp.rabbitmq.host";
        static final String AMQP_RABBITMQ_USERNAME = "amqp.rabbitmq.username";
        static final String AMQP_RABBITMQ_PASSWORD = "amqp.rabbitmq.password";
        static final String AMQP_STREAM_PORT = "amqp.stream.port";
        static final String AMQP_STREAM_CRYPTO_BYBIT = "amqp.stream.crypto.bybit";
        static final String AMQP_STREAM_METRICS_BYBIT = "amqp.stream.metrics.bybit";
        static final String AMQP_STREAM_METRICS_CMC = "amqp.stream.metrics.cmc";
        static final String AMQP_EXCHANGE_COLLECTOR = "amqp.exchange.collector";
        static final String AMQP_QUEUE_COLLECTOR = "amqp.queue.collector";
        static final String AMQP_RABBITMQ_PORT = "amqp.rabbitmq.port";
    }

    final static class JdbcConfig {
        private JdbcConfig() {
            throw new UnsupportedOperationException();
        }

        static final String JDBC_URL = "jdbc.datasource.url";
        static final String JDBC_USERNAME = "jdbc.datasource.username";
        static final String JDBC_PASSWORD = "jdbc.datasource.password";
        static final String JDBC_CMC_BATCH_SIZE = "jdbc.cmc.batch-size";
        static final String JDBC_BYBIT_BATCH_SIZE = "jdbc.bybit.batch-size";
        static final String JDBC_BYBIT_FLUSH_INTERVAL_MS = "jdbc.bybit.flush-interval-ms";
    }

    final static class ServerConfig {
        private ServerConfig() {
            throw new UnsupportedOperationException();
        }

        static final String SERVER_PORT = "server.port";
    }
}
