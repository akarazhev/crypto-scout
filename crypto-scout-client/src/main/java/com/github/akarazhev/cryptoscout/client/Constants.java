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

package com.github.akarazhev.cryptoscout.client;

final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    final static class AMQP {
        private AMQP() {
            throw new UnsupportedOperationException();
        }
        // General settings
        static final String CONTENT_TYPE = "application/json";
        static final int DELIVERY_MODE = 2;
        static final String TOPIC = "topic";
        static final String STREAM = "stream";
        static final String X_MESSAGE_TTL = "x-message-ttl";
        static final String X_MAX_LENGTH = "x-max-length";
        static final String X_QUEUE_TYPE = "x-queue-type";
        static final String X_MAX_LENGTH_BYTES = "x-max-length-bytes";
        static final String X_STREAM_MAX_SEGMENT_SIZE_BYTES = "x-stream-max-segment-size-bytes";
        // Dead letter
        static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
        static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
        static final String X_DEAD_LETTER_EXCHANGE_VALUE = "";
        // Routing keys
        static final String ROUTING_KEY_METRICS_CMC = "metrics.cmc";
        static final String ROUTING_KEY_METRICS_BYBIT = "metrics.bybit";
        static final String ROUTING_KEY_CRYPTO_BYBIT = "crypto.bybit";
        static final String ROUTING_KEY_COLLECTOR = "collector";
        static final String ROUTING_KEY_TELEGRAM = "telegram";
    }
}
