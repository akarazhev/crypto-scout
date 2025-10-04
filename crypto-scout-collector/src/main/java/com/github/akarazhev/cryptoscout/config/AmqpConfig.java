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

import com.github.akarazhev.jcryptolib.config.AppConfig;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.stream.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_EXCHANGE_COLLECTOR;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_COLLECTOR;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_HOST;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PASSWORD;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PORT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_USERNAME;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_CRYPTO_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_METRICS_CMC;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_PORT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.CONNECTION_NAME;

public final class AmqpConfig {
    private AmqpConfig() {
        throw new UnsupportedOperationException();
    }

    public static String getAmqpRabbitmqHost() {
        return AppConfig.getAsString(AMQP_RABBITMQ_HOST);
    }

    public static String getAmqpRabbitmqUsername() {
        return AppConfig.getAsString(AMQP_RABBITMQ_USERNAME);
    }

    public static String getAmqpRabbitmqPassword() {
        return AppConfig.getAsString(AMQP_RABBITMQ_PASSWORD);
    }

    private static int getAmqpStreamPort() {
        return AppConfig.getAsInt(AMQP_STREAM_PORT);
    }

    public static String getAmqpStreamCryptoBybit() {
        return AppConfig.getAsString(AMQP_STREAM_CRYPTO_BYBIT);
    }

    public static String getAmqpStreamMetricsBybit() {
        return AppConfig.getAsString(AMQP_STREAM_METRICS_BYBIT);
    }

    public static String getAmqpStreamMetricsCmc() {
        return AppConfig.getAsString(AMQP_STREAM_METRICS_CMC);
    }

    public static String getAmqpExchangeCollector() {
        return AppConfig.getAsString(AMQP_EXCHANGE_COLLECTOR);
    }

    public static String getAmqpQueueCollector() {
        return AppConfig.getAsString(AMQP_QUEUE_COLLECTOR);
    }

    public static int getAmqpRabbitmqPort() {
        return AppConfig.getAsInt(AMQP_RABBITMQ_PORT);
    }

    public static Connection getConnection() throws IOException, TimeoutException {
        final var factory = new ConnectionFactory();
        factory.setHost(AmqpConfig.getAmqpRabbitmqHost());
        factory.setPort(AmqpConfig.getAmqpRabbitmqPort());
        factory.setUsername(AmqpConfig.getAmqpRabbitmqUsername());
        factory.setPassword(AmqpConfig.getAmqpRabbitmqPassword());
        return factory.newConnection(CONNECTION_NAME);
    }

    public static Environment getEnvironment() {
        return Environment.builder()
                .host(AmqpConfig.getAmqpRabbitmqHost())
                .port(AmqpConfig.getAmqpStreamPort())
                .username(AmqpConfig.getAmqpRabbitmqUsername())
                .password(AmqpConfig.getAmqpRabbitmqPassword())
                .build();
    }
}
