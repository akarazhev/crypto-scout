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

package com.github.akarazhev.cryptoscout.util;

import com.github.akarazhev.jcryptolib.stream.Payload;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utility class for enhanced logging with MDC (Mapped Diagnostic Context) support.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Set up and clear MDC context for structured logging</li>
 *   <li>Execute code blocks with proper MDC context</li>
 *   <li>Handle exceptions and ensure MDC context is always cleared</li>
 *   <li>Generate correlation IDs for tracking operations across log entries</li>
 * </ul>
 * <p>
 * MDC allows attaching metadata to log messages, making it easier to trace and filter
 * related log entries in a distributed system.
 */
public final class LoggingUtils {

    private LoggingUtils() {
        // Utility class, no instantiation
    }

    /**
     * Constants for MDC keys used throughout the application.
     * <p>
     * These keys are used to store contextual information in the MDC,
     * which is then included in log messages.
     */
    public static final class MdcKeys {
        /** Unique identifier for tracking related log entries */
        public static final String CORRELATION_ID = "correlationId";
        
        /** Data provider (e.g., "BYBIT", "CMC") */
        public static final String PROVIDER = "provider";
        
        /** Source of the data */
        public static final String SOURCE = "source";
        
        /** Current operation being performed */
        public static final String OPERATION = "operation";
        
        private MdcKeys() {
            // Constants class, no instantiation
        }
    }

    /**
     * Executes a function with MDC context for a data payload.
     * <p>
     * This method sets up MDC context with information from the payload,
     * executes the provided runnable, and ensures the MDC context is cleared
     * regardless of whether the runnable completes normally or throws an exception.
     *
     * @param logger The logger to use for logging
     * @param payload The data payload containing contextual information
     * @param operation The operation being performed
     * @param runnable The code to execute with MDC context
     */
    public static void withMdc(Logger logger, Payload<Map<String, Object>> payload, String operation, Runnable runnable) {
        String correlationId = UUID.randomUUID().toString();
        
        try {
            MDC.put(MdcKeys.CORRELATION_ID, correlationId);
            MDC.put(MdcKeys.PROVIDER, payload.getProvider().toString());
            MDC.put(MdcKeys.SOURCE, payload.getSource().toString());
            MDC.put(MdcKeys.OPERATION, operation);
            
            logger.debug("Starting operation: {}", operation);
            runnable.run();
            logger.debug("Completed operation: {}", operation);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.PROVIDER);
            MDC.remove(MdcKeys.SOURCE);
            MDC.remove(MdcKeys.OPERATION);
        }
    }

    /**
     * Executes a function with MDC context for a data payload and returns a result.
     * <p>
     * Similar to {@link #withMdc(Logger, Payload, String, Runnable)}, but allows
     * the executed code to return a value.
     *
     * @param logger The logger to use for logging
     * @param payload The data payload containing contextual information
     * @param operation The operation being performed
     * @param supplier The code to execute with MDC context
     * @param <T> The return type
     * @return The result of the supplier
     */
    public static <T> T withMdcFunction(Logger logger, Payload<Map<String, Object>> payload, String operation, Supplier<T> supplier) {
        String correlationId = UUID.randomUUID().toString();
        
        try {
            MDC.put(MdcKeys.CORRELATION_ID, correlationId);
            MDC.put(MdcKeys.PROVIDER, payload.getProvider().toString());
            MDC.put(MdcKeys.SOURCE, payload.getSource().toString());
            MDC.put(MdcKeys.OPERATION, operation);
            
            logger.debug("Starting operation: {}", operation);
            T result = supplier.get();
            logger.debug("Completed operation: {}", operation);
            return result;
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.PROVIDER);
            MDC.remove(MdcKeys.SOURCE);
            MDC.remove(MdcKeys.OPERATION);
        }
    }

    /**
     * Executes a function with MDC context for general operations and returns a result.
     * <p>
     * This method is similar to {@link #withMdcFunction(Logger, Payload, String, Supplier)},
     * but doesn't require a Payload object, making it suitable for general operations.
     *
     * @param logger The logger to use for logging
     * @param operation The operation being performed
     * @param supplier The code to execute with MDC context
     * @param <T> The return type
     * @return The result of the supplier
     */
    public static <T> T withMdcFunction(Logger logger, String operation, Supplier<T> supplier) {
        String correlationId = setupMdc(operation);
        try {
            logger.debug("Starting operation: {}", operation);
            T result = supplier.get();
            logger.debug("Completed operation: {}", operation);
            return result;
        } catch (Exception e) {
            logger.error("Error during operation: {}, correlationId: {}", operation, correlationId, e);
            throw e;
        } finally {
            clearMdc();
        }
    }

    /**
     * Executes a function with MDC context for general operations and handles exceptions.
     * <p>
     * This method allows the executed code to throw checked exceptions,
     * which are then propagated to the caller.
     *
     * @param operation The operation being performed
     * @param logger The logger to use for error reporting
     * @param executable The code to execute with MDC context
     * @param <E> The type of exception that might be thrown
     * @throws E if the executable throws an exception
     */
    public static <E extends Exception> void withMdcOperation(String operation, Logger logger, ThrowingRunnable<E> executable) throws E {
        String correlationId = setupMdc(operation);
        try {
            executable.run();
        } catch (Exception e) {
            logger.error("Error during operation: {}, correlationId: {}", operation, correlationId, e);
            throw (E) e;
        } finally {
            clearMdc();
        }
    }

    /**
     * Executes a function with MDC context for general operations, catches all exceptions and logs them.
     * <p>
     * This method is similar to {@link #withMdcOperation(String, Logger, ThrowingRunnable)},
     * but catches and logs any exceptions instead of propagating them.
     *
     * @param operation The operation being performed
     * @param logger The logger to use for error reporting
     * @param executable The code to execute with MDC context
     */
    public static void withMdcOperationSafe(String operation, Logger logger, Runnable executable) {
        String correlationId = setupMdc(operation);
        try {
            executable.run();
        } catch (Exception e) {
            logger.error("Error during operation: {}, correlationId: {}", operation, correlationId, e);
        } finally {
            clearMdc();
        }
    }

    /**
     * Functional interface for operations that might throw exceptions.
     *
     * @param <E> The type of exception that might be thrown
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        /**
         * Executes the operation that might throw an exception.
         *
         * @throws E if an error occurs during execution
         */
        void run() throws E;
    }

    /**
     * Sets up MDC context for general operations.
     * <p>
     * This method generates a correlation ID and sets up basic MDC context.
     *
     * @param operation The operation being performed
     * @return The correlation ID generated for this context
     */
    public static String setupMdc(String operation) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.OPERATION, operation);
        return correlationId;
    }

    /**
     * Clears the MDC context.
     * <p>
     * This method should be called in a finally block to ensure
     * the MDC context is always cleared, even if an exception occurs.
     */
    public static void clearMdc() {
        MDC.clear();
    }
}
