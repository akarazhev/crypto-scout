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

package com.github.akarazhev.cryptoscout.stream.bybit;

import com.github.akarazhev.jcryptolib.DataStreams;
import com.github.akarazhev.jcryptolib.bybit.config.StreamType;
import com.github.akarazhev.jcryptolib.bybit.config.Topic;
import com.github.akarazhev.jcryptolib.bybit.stream.DataConfig;
import com.github.akarazhev.jcryptolib.stream.Payload;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.Map;

import static com.github.akarazhev.jcryptolib.bybit.config.Type.LPL;

@Component
final class BybitDataSupplier {
    private final HttpClient client;

    public BybitDataSupplier(final HttpClient client) {
        this.client = client;
    }

    public Flowable<Payload<Map<String, Object>>> events() {
        final var config = new DataConfig.Builder()
                .type(LPL)
                .build();
        return DataStreams.ofBybit(client, config);
    }

    public Flowable<Payload<Map<String, Object>>> publicSpotTradeData() {
        final var config = new DataConfig.Builder()
                .streamType(StreamType.PTST)
                .topic(Topic.PUBLIC_TRADE_BTC_USDT)
                .topic(Topic.PUBLIC_TRADE_ETH_USDT)
                .build();
        return DataStreams.ofBybit(client, config);
    }
}
