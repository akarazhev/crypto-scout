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
import com.github.akarazhev.cryptoscout.config.MetricsConfig;
import com.github.akarazhev.cryptoscout.util.LoggingUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The DataBridge class serves as a bridge between data streams and data publishers.
 * It manages the lifecycle of reactive data streams, handles subscriptions, and
 * ensures proper resource cleanup. This class is responsible for:
 * <ul>
 *   <li>Starting and stopping data streams</li>
 *   <li>Managing subscriptions to data sources</li>
 *   <li>Publishing received data to appropriate destinations</li>
 *   <li>Tracking metrics for data processing</li>
 *   <li>Providing graceful shutdown capabilities</li>
 * </ul>
 */
@Component
public final class DataBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBridge.class);
    
    private final DataStream dataStream;
    private final DataPublisher dataPublisher;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final MetricsConfig metricsConfig;
    private final Timer processingTimer;

    /**
     * Constructs a new DataBridge with the specified components.
     *
     * @param dataStream The data stream source that provides data
     * @param dataPublisher The publisher that sends data to destinations
     * @param metricsConfig The configuration for metrics collection
     * @param meterRegistry The registry for recording metrics
     */
    public DataBridge(final DataStream dataStream, final DataPublisher dataPublisher, 
                     final MetricsConfig metricsConfig, final MeterRegistry meterRegistry) {
        this.dataStream = dataStream;
        this.dataPublisher = dataPublisher;
        this.metricsConfig = metricsConfig;
        this.processingTimer = meterRegistry.timer("crypto.scout.data.processing.duration");
    }

    /**
     * Starts the data stream and subscribes to it.
     * This method:
     * <ul>
     *   <li>Creates a subscription to the data stream</li>
     *   <li>Sets up logging and metrics for the subscription lifecycle</li>
     *   <li>Processes and publishes received data</li>
     *   <li>Handles errors in the data stream</li>
     * </ul>
     * If the bridge is already active, this method will have no effect.
     */
    public void start() {
        LoggingUtils.withMdcOperationSafe("data_bridge_start", LOGGER, () -> {
            LOGGER.info("Starting data bridge");
            
            Disposable subscription = dataStream.data()
                    .doOnSubscribe(d -> {
                        MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                        LOGGER.info("Data stream subscription started");
                        metricsConfig.getActiveDataStreams().incrementAndGet();
                    })
                    .doOnComplete(() -> {
                        MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                        LOGGER.info("Data stream completed");
                    })
                    .doOnError(e -> {
                        MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                        LOGGER.error("Error in data stream", e);
                    })
                    .doFinally(() -> {
                        MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                        LOGGER.info("Data stream subscription finalized");
                    })
                    .subscribe(
                        payload -> {
                            Timer.Sample sample = Timer.start();
                            try {
                                MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                                metricsConfig.getDataPointsProcessed().incrementAndGet();
                                dataPublisher.publish(payload);
                            } finally {
                                sample.stop(processingTimer);
                            }
                        },
                        error -> {
                            MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                            LOGGER.error("Error in data stream subscription", error);
                        },
                        () -> {
                            MDC.put(LoggingUtils.MdcKeys.OPERATION, "data_bridge_start");
                            LOGGER.info("Data stream subscription completed");
                        }
                    );
                    
            disposables.add(subscription);
            active.set(true);
            LOGGER.info("Data bridge started successfully");
        });
    }

    /**
     * Stops the data stream and releases resources.
     * This method:
     * <ul>
     *   <li>Disposes all active subscriptions</li>
     *   <li>Updates metrics to reflect the stopped state</li>
     *   <li>Signals completion via the shutdown latch</li>
     * </ul>
     * If the bridge is already stopped, this method will have no effect.
     */
    public void stop() {
        LoggingUtils.withMdcOperationSafe("data_bridge_stop", LOGGER, () -> {
            if (active.getAndSet(false)) {
                LOGGER.info("Stopping data bridge");
                
                try {
                    // Dispose all subscriptions
                    disposables.dispose();
                    
                    // Signal that shutdown is complete
                    shutdownLatch.countDown();
                    
                    // Update metrics
                    metricsConfig.getActiveDataStreams().decrementAndGet();
                    
                    LOGGER.info("Data bridge stopped successfully");
                } catch (Exception e) {
                    LOGGER.error("Error stopping data bridge", e);
                }
            } else {
                LOGGER.info("Data bridge is already stopped");
            }
        });
    }

    /**
     * Restarts the data stream.
     * This is equivalent to calling {@link #stop()} followed by {@link #start()}.
     * This method is useful when the data stream needs to be refreshed or
     * when recovering from an error state.
     */
    public void restart() {
        LoggingUtils.withMdcOperationSafe("data_bridge_restart", LOGGER, () -> {
            LOGGER.info("Restarting data bridge");
            stop();
            start();
            LOGGER.info("Data bridge restarted successfully");
        });
    }
    
    /**
     * Checks if the data bridge is currently active.
     * The bridge is considered active if it has been started and its
     * subscriptions have not been disposed.
     *
     * @return true if the data stream is active and processing, false otherwise
     */
    public boolean isActive() {
        return active.get() && !disposables.isDisposed();
    }
    
    /**
     * Waits for the data bridge to terminate.
     * <p>
     * This method blocks until one of the following happens:
     * <ul>
     *   <li>The data bridge terminates</li>
     *   <li>The timeout is reached</li>
     *   <li>The thread is interrupted</li>
     * </ul>
     * 
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the shutdown completed before the timeout, false otherwise
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            return LoggingUtils.withMdcFunction(LOGGER, "data_bridge_await_termination", () -> {
                try {
                    LOGGER.info("Awaiting termination of data bridge for {} {}", timeout, unit);
                    boolean result = shutdownLatch.await(timeout, unit);
                    LOGGER.info("Data bridge termination completed: {}", result);
                    return result;
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted while awaiting termination of data bridge");
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    throw new RuntimeException(e); // Will be caught by withMdcFunction
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            }
            throw e;
        }
    }
}
