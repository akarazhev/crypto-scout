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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * A resilient service that wraps data supplier operations with circuit breaker and retry patterns.
 */
@Service
public class ResilientDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientDataService.class);
    private final DataSupplier dataSupplier;

    public ResilientDataService(DataSupplier dataSupplier) {
        this.dataSupplier = dataSupplier;
    }

    /**
     * Retrieves CoinMarketCap data with circuit breaker and retry mechanisms.
     *
     * @return A flowable of payload data
     */
    @CircuitBreaker(name = "cmcDataService", fallbackMethod = "fallbackCmcData")
    @Retry(name = "cmcDataService")
    public Flowable<Payload<Map<String, Object>>> getCmcData() {
        LOGGER.info("Retrieving CoinMarketCap data");
        return dataSupplier.ofCmcData();
    }

    /**
     * Retrieves Bybit events with circuit breaker and retry mechanisms.
     *
     * @return A flowable of payload data
     */
    @CircuitBreaker(name = "cmcDataService", fallbackMethod = "fallbackBybitEvents")
    @Retry(name = "cmcDataService")
    public Flowable<Payload<Map<String, Object>>> getBybitEvents() {
        LOGGER.info("Retrieving Bybit events");
        return dataSupplier.ofBybitEvents();
    }

    /**
     * Fallback method for CoinMarketCap data retrieval.
     *
     * @param e The exception that triggered the fallback
     * @return An empty flowable
     */
    private Flowable<Payload<Map<String, Object>>> fallbackCmcData(Exception e) {
        LOGGER.error("Circuit breaker triggered for CoinMarketCap data: {}", e.getMessage());
        return Flowable.empty();
    }

    /**
     * Fallback method for Bybit events retrieval.
     *
     * @param e The exception that triggered the fallback
     * @return An empty flowable
     */
    private Flowable<Payload<Map<String, Object>>> fallbackBybitEvents(Exception e) {
        LOGGER.error("Circuit breaker triggered for Bybit events: {}", e.getMessage());
        return Flowable.empty();
    }
}
