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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for custom business metrics.
 */
@Configuration
public class MetricsConfig {

    /**
     * The number of data points processed.
     */
    private final AtomicLong dataPointsProcessed = new AtomicLong(0);
    
    /**
     * The number of data points published.
     */
    private final AtomicLong dataPointsPublished = new AtomicLong(0);
    
    /**
     * The number of API requests received.
     */
    private final AtomicLong apiRequestsReceived = new AtomicLong(0);
    
    /**
     * The number of active data streams.
     */
    private final AtomicInteger activeDataStreams = new AtomicInteger(0);
    
    /**
     * The number of failed API calls.
     */
    private final AtomicLong failedApiCalls = new AtomicLong(0);

    /**
     * Registers custom business metrics with the meter registry.
     *
     * @param registry the meter registry
     * @return the configured meter registry
     */
    @Bean
    public MeterRegistry registerMetrics(MeterRegistry registry) {
        // Register gauges
        Gauge.builder("crypto.scout.data.streams.active", activeDataStreams, AtomicInteger::get)
                .description("Number of active data streams")
                .register(registry);
                
        // Register counters
        Counter.builder("crypto.scout.data.points.processed")
                .description("Number of data points processed")
                .register(registry);
                
        Counter.builder("crypto.scout.data.points.published")
                .description("Number of data points published")
                .register(registry);
                
        Counter.builder("crypto.scout.api.requests")
                .description("Number of API requests received")
                .register(registry);
                
        Counter.builder("crypto.scout.api.calls.failed")
                .description("Number of failed API calls")
                .register(registry);
                
        // Register timers
        Timer.builder("crypto.scout.api.request.duration")
                .description("API request duration")
                .register(registry);
                
        Timer.builder("crypto.scout.data.processing.duration")
                .description("Data processing duration")
                .register(registry);
                
        return registry;
    }

    /**
     * Gets the data points processed counter.
     *
     * @return the data points processed
     */
    public AtomicLong getDataPointsProcessed() {
        return dataPointsProcessed;
    }

    /**
     * Gets the data points published counter.
     *
     * @return the data points published
     */
    public AtomicLong getDataPointsPublished() {
        return dataPointsPublished;
    }

    /**
     * Gets the API requests received counter.
     *
     * @return the API requests received
     */
    public AtomicLong getApiRequestsReceived() {
        return apiRequestsReceived;
    }

    /**
     * Gets the active data streams gauge.
     *
     * @return the active data streams
     */
    public AtomicInteger getActiveDataStreams() {
        return activeDataStreams;
    }

    /**
     * Gets the failed API calls counter.
     *
     * @return the failed API calls
     */
    public AtomicLong getFailedApiCalls() {
        return failedApiCalls;
    }
}
