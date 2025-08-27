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

package com.github.akarazhev.cryptoscout.stream;

import com.github.akarazhev.cryptoscout.amqp.DataPublisher;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.stereotype.Component;

@Component
public final class DataBridge {
    private final DataStreams dataStreams;
    private final DataPublisher dataPublisher;
    private Disposable cmcDataStream;
    private Disposable bybitEventStream;
    private Disposable bybitPublicSpotTradeStream;

    public DataBridge(final DataStreams dataStreams, final DataPublisher dataPublisher) {
        this.dataStreams = dataStreams;
        this.dataPublisher = dataPublisher;
    }

    public void start() {
        cmcDataStream = dataStreams.of(DataStreams.Type.CMC_DATA_STREAM)
                .stream()
                .subscribe(dataPublisher::publish);
        bybitEventStream = dataStreams.of(DataStreams.Type.BYBIT_EVENT_STREAM)
                .stream()
                .subscribe(dataPublisher::publish);
        bybitPublicSpotTradeStream = dataStreams.of(DataStreams.Type.BYBIT_PUBLIC_SPOT_TRADE_STREAM)
                .stream()
                .subscribe(dataPublisher::publish);
    }

    public void stop() {
        if (cmcDataStream != null) {
            cmcDataStream.dispose();
        }

        if (bybitEventStream != null) {
            bybitEventStream.dispose();
        }

        if (bybitPublicSpotTradeStream != null) {
            bybitPublicSpotTradeStream.dispose();
        }
    }

    public void restart() {
        stop();
        start();
    }
}
