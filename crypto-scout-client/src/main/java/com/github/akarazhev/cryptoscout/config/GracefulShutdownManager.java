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

import com.github.akarazhev.cryptoscout.stream.DataBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Manages graceful shutdown of application resources.
 * Ensures that active connections and streams are properly closed before the application exits.
 */
@Component
public class GracefulShutdownManager implements ApplicationListener<ContextClosedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownManager.class);

    private final DataBridge dataBridge;
    private final ConnectionFactory connectionFactory;
    
    @Value("${shutdown.wait.seconds:10}")
    private int shutdownWaitSeconds;

    public GracefulShutdownManager(DataBridge dataBridge, ConnectionFactory connectionFactory) {
        this.dataBridge = dataBridge;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOGGER.info("Application shutdown initiated. Starting graceful shutdown sequence...");
        
        try {
            // Step 1: Stop data streams
            LOGGER.info("Stopping data streams...");
            dataBridge.stop();
            
            // Step 2: Wait for processing to complete
            LOGGER.info("Waiting for processing to complete (max {} seconds)...", shutdownWaitSeconds);
            TimeUnit.SECONDS.sleep(Math.min(shutdownWaitSeconds, 2)); // Small wait for streams to finish
            
            // Step 3: Close RabbitMQ connections
            LOGGER.info("Closing RabbitMQ connections...");
            if (connectionFactory instanceof AutoCloseable) {
                ((AutoCloseable) connectionFactory).close();
            }
            
            LOGGER.info("Graceful shutdown completed successfully");
        } catch (Exception e) {
            LOGGER.error("Error during graceful shutdown", e);
        }
    }
}
