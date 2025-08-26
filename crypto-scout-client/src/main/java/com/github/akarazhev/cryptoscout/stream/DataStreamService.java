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

import com.github.akarazhev.jcryptolib.stream.Payload;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
final class DataStreamService implements DataStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataStreamService.class);
    private final DataSupplier dataSupplier;
    @Value("${cmc.retry.base.ms:1000}")
    private long retryBaseMs;
    @Value("${cmc.retry.max.ms:60000}")
    private long retryMaxMs;
    @Value("${cmc.retry.jitter:0.2}")
    private double retryJitter;

    public DataStreamService(final DataSupplier dataSupplier) {
        this.dataSupplier = dataSupplier;
    }

    @Override
    public Flowable<Payload<Map<String, Object>>> data() {
        return Flowable.defer(() ->
                        dataSupplier.ofCmcData()
                                .doOnSubscribe(s -> LOGGER.info("CMC data stream subscribed"))
                                .doOnError(e -> LOGGER.error("CMC data stream error", e))
                                .onErrorResumeNext((Throwable t) -> Flowable.empty())
                )
                .repeatWhen(completed ->
                        completed
                                .zipWith(Flowable.range(1, Integer.MAX_VALUE), (ignored, attempt) -> attempt)
                                .flatMap(attempt -> {
                                    final var delay = computeBackoffDelayMs(attempt, retryBaseMs, retryMaxMs, retryJitter);
                                    LOGGER.warn("Resubscribing to CMC stream in {} ms (attempt #{})", delay, attempt);
                                    return Flowable.timer(delay, TimeUnit.MILLISECONDS);
                                })
                )
                .subscribeOn(Schedulers.io())
                .doOnCancel(() -> LOGGER.info("Data stream cancelled"));
    }

    private long computeBackoffDelayMs(final int attempt, final long baseMs, final long maxMs, final double jitterFactor) {
        // Exponential backoff with cap and jitter
        final var exp = baseMs * Math.pow(2.0, Math.max(0, attempt - 1));
        final var delay = Math.min((long) exp, maxMs);
        final var jitter = (long) (delay * jitterFactor);
        return delay + ThreadLocalRandom.current().nextLong(0, jitter + 1);
    }
}
