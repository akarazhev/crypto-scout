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
import com.rabbitmq.stream.Environment;

import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_STREAM_PORT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_HOST;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PASSWORD;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_USERNAME;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_CRYPTO_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_METRICS_CMC;

public final class AmqpConfig {
    private AmqpConfig() {
        throw new UnsupportedOperationException();
    }

    private static String getAmqpRabbitmqHost() {
        return AppConfig.getAsString(AMQP_RABBITMQ_HOST);
    }

    private static String getAmqpRabbitmqUsername() {
        return AppConfig.getAsString(AMQP_RABBITMQ_USERNAME);
    }

    private static String getAmqpRabbitmqPassword() {
        return AppConfig.getAsString(AMQP_RABBITMQ_PASSWORD);
    }

    private static int getAmqpRabbitmqStreamPort() {
        return AppConfig.getAsInt(AMQP_RABBITMQ_STREAM_PORT);
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

    public static Environment getEnvironment() {
        return Environment.builder()
                .host(AmqpConfig.getAmqpRabbitmqHost())
                .port(AmqpConfig.getAmqpRabbitmqStreamPort())
                .username(AmqpConfig.getAmqpRabbitmqUsername())
                .password(AmqpConfig.getAmqpRabbitmqPassword())
                .build();
    }
}
