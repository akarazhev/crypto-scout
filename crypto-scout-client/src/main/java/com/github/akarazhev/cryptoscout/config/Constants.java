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

        static final String CONNECTION_NAME = "crypto-scout-client";
        static final String AMQP_EXCHANGE_METRICS = "amqp.exchange.metrics";
        static final String AMQP_EXCHANGE_CRYPTO = "amqp.exchange.crypto";
        static final String AMQP_RABBITMQ_HOST = "amqp.rabbitmq.host";
        static final String AMQP_RABBITMQ_PORT = "amqp.rabbitmq.port";
        static final String AMQP_RABBITMQ_USERNAME = "amqp.rabbitmq.username";
        static final String AMQP_RABBITMQ_PASSWORD = "amqp.rabbitmq.password";
    }

    final static class ServerConfig {
        private ServerConfig() {
            throw new UnsupportedOperationException();
        }

        static final String SERVER_PORT = "server.port";
    }
}
