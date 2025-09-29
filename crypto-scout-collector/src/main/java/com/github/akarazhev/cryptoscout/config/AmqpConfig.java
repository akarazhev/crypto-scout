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

import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_EXCHANGE_COLLECTOR;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_EXCHANGE_CRYPTO;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_EXCHANGE_METRICS;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_CMC;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_COLLECTOR;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_DEAD;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_MAX_LENGTH;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_QUEUE_TTL_MS;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_HOST;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PASSWORD;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_PORT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_RABBITMQ_USERNAME;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_BYBIT;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_MAX_BYTES;
import static com.github.akarazhev.cryptoscout.config.Constants.AmqpConfig.AMQP_STREAM_SEGMENT_BYTES;

public final class AmqpConfig {
    private AmqpConfig() {
        throw new UnsupportedOperationException();
    }

    public static String getAmqpExchangeMetrics() {
        return AppConfig.getAsString(AMQP_EXCHANGE_METRICS);
    }

    public static String getAmqpExchangeCrypto() {
        return AppConfig.getAsString(AMQP_EXCHANGE_CRYPTO);
    }

    public static String getAmqpExchangeCollector() {
        return AppConfig.getAsString(AMQP_EXCHANGE_COLLECTOR);
    }

    public static String getAmqpQueueCmc() {
        return AppConfig.getAsString(AMQP_QUEUE_CMC);
    }

    public static String getAmqpQueueBybit() {
        return AppConfig.getAsString(AMQP_QUEUE_BYBIT);
    }

    public static String getAmqpQueueDead() {
        return AppConfig.getAsString(AMQP_QUEUE_DEAD);
    }

    public static String getAmqpQueueCollector() {
        return AppConfig.getAsString(AMQP_QUEUE_COLLECTOR);
    }

    public static String getAmqpStreamBybit() {
        return AppConfig.getAsString(AMQP_STREAM_BYBIT);
    }

    public static int getAmqpQueueTtlMs() {
        return AppConfig.getAsInt(AMQP_QUEUE_TTL_MS);
    }

    public static int getAmqpQueueMaxLength() {
        return AppConfig.getAsInt(AMQP_QUEUE_MAX_LENGTH);
    }

    public static int getAmqpStreamMaxBytes() {
        return AppConfig.getAsInt(AMQP_STREAM_MAX_BYTES);
    }

    public static int getAmqpStreamSegmentBytes() {
        return AppConfig.getAsInt(AMQP_STREAM_SEGMENT_BYTES);
    }

    public static String getAmqpRabbitmqHost() {
        return AppConfig.getAsString(AMQP_RABBITMQ_HOST);
    }

    public static int getAmqpRabbitmqPort() {
        return AppConfig.getAsInt(AMQP_RABBITMQ_PORT);
    }

    public static String getAmqpRabbitmqUsername() {
        return AppConfig.getAsString(AMQP_RABBITMQ_USERNAME);
    }

    public static String getAmqpRabbitmqPassword() {
        return AppConfig.getAsString(AMQP_RABBITMQ_PASSWORD);
    }
}
