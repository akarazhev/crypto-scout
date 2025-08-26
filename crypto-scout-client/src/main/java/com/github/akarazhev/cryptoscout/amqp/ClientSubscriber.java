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

package com.github.akarazhev.cryptoscout.amqp;

import com.github.akarazhev.cryptoscout.Command;
import com.github.akarazhev.cryptoscout.Subscriber;
import com.github.akarazhev.cryptoscout.stream.DataBridge;
import com.github.akarazhev.cryptoscout.util.CommandValidator;
import com.github.akarazhev.cryptoscout.util.LoggingUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * The ClientSubscriber class is responsible for receiving and processing commands from RabbitMQ.
 * It implements the Subscriber interface and handles the lifecycle of the DataBridge component.
 * <p>
 * This class:
 * <ul>
 *   <li>Listens for commands on a configured RabbitMQ queue</li>
 *   <li>Validates incoming commands</li>
 *   <li>Executes appropriate actions based on command type</li>
 *   <li>Manages the DataBridge lifecycle during application startup and shutdown</li>
 *   <li>Provides structured logging with MDC context</li>
 * </ul>
 */
@Service
public final class ClientSubscriber implements Subscriber<Command<Object>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSubscriber.class);
    private final DataBridge dataBridge;

    /**
     * Constructs a new ClientSubscriber with the specified DataBridge.
     *
     * @param dataBridge The DataBridge instance to manage
     */
    public ClientSubscriber(final DataBridge dataBridge) {
        this.dataBridge = dataBridge;
    }

    /**
     * Processes commands received from the RabbitMQ queue.
     * This method validates the incoming command and executes the appropriate action
     * based on the command type (START, STOP, RESTART).
     * <p>
     * All operations are performed with proper MDC context for logging and error handling.
     *
     * @param command The command to process
     */
    @RabbitListener(queues = "${amqp.queue.client}")
    @Override
    public void subscribe(final Command<Object> command) {
        LoggingUtils.withMdcOperationSafe("command_processing", LOGGER, () -> {
            LOGGER.info("Received command: {}", command);
            
            try {
                // Validate the command
                CommandValidator.validateOrThrow(command);
                
                switch (command.action()) {
                    case STOP -> {
                        LOGGER.info("Executing STOP command");
                        dataBridge.stop();
                        LOGGER.info("STOP command executed successfully");
                    }
                    case START -> {
                        LOGGER.info("Executing START command");
                        dataBridge.start();
                        LOGGER.info("START command executed successfully");
                    }
                    case RESTART -> {
                        LOGGER.info("Executing RESTART command");
                        dataBridge.restart();
                        LOGGER.info("RESTART command executed successfully");
                    }
                    default -> {
                        LOGGER.warn("Unknown command action: {}", command.action());
                    }
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                LOGGER.error("Invalid command received: {}", command, e);
            } catch (Exception e) {
                LOGGER.error("Error processing command: {}", command, e);
            }
        });
    }

    /**
     * Stops the DataBridge during application shutdown.
     * This method is called automatically when the Spring container is shutting down.
     */
    @PreDestroy
    public void stop() {
        LoggingUtils.withMdcOperationSafe("lifecycle_stop", LOGGER, () -> {
            LOGGER.info("Stopping data bridge during application shutdown");
            dataBridge.stop();
            LOGGER.info("Data bridge stopped successfully");
        });
    }

    /**
     * Starts the DataBridge during application startup.
     * This method is called automatically after the bean has been constructed.
     */
    @PostConstruct
    public void start() {
        LoggingUtils.withMdcOperationSafe("lifecycle_start", LOGGER, () -> {
            LOGGER.info("Starting data bridge during application startup");
            dataBridge.start();
            LOGGER.info("Data bridge started successfully");
        });
    }
}
