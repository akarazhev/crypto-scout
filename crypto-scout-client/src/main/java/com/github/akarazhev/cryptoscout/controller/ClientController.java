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

package com.github.akarazhev.cryptoscout.controller;

import com.github.akarazhev.cryptoscout.config.MetricsConfig;
import com.github.akarazhev.cryptoscout.stream.DataBridge;
import com.github.akarazhev.cryptoscout.util.LoggingUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing the crypto scout client.
 */
@RestController
@RequestMapping("/api/client")
@Tag(name = "Client Controller", description = "API endpoints for managing the crypto scout client")
public class ClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
    
    private final DataBridge dataBridge;
    private final MetricsConfig metricsConfig;
    private final Timer requestTimer;

    public ClientController(DataBridge dataBridge, MetricsConfig metricsConfig, MeterRegistry meterRegistry) {
        this.dataBridge = dataBridge;
        this.metricsConfig = metricsConfig;
        this.requestTimer = meterRegistry.timer("crypto.scout.api.request.duration");
    }

    /**
     * Get the status of the data bridge.
     *
     * @return the status response
     */
    @GetMapping("/status")
    @Operation(summary = "Get client status", description = "Returns the current status of the data bridge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getStatus() {
        String correlationId = LoggingUtils.setupMdc("get_client_status");
        Timer.Sample sample = Timer.start();
        
        try {
            LOGGER.info("Getting client status");
            metricsConfig.getApiRequestsReceived().incrementAndGet();
            
            Map<String, Object> status = new HashMap<>();
            status.put("active", dataBridge.isActive());
            status.put("timestamp", System.currentTimeMillis());
            
            // Add metrics to the response
            status.put("metrics", getMetricsData());
            
            LOGGER.info("Client status retrieved: {}", status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            LOGGER.error("Error getting client status", e);
            metricsConfig.getFailedApiCalls().incrementAndGet();
            throw e;
        } finally {
            sample.stop(requestTimer);
            LoggingUtils.clearMdc();
        }
    }

    /**
     * Start the data bridge.
     *
     * @return the response entity
     */
    @PostMapping("/start")
    @Operation(summary = "Start client", description = "Starts the data bridge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client started successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> startClient() {
        String correlationId = LoggingUtils.setupMdc("start_client");
        Timer.Sample sample = Timer.start();
        
        try {
            LOGGER.info("Starting client");
            metricsConfig.getApiRequestsReceived().incrementAndGet();
            
            dataBridge.start();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "started");
            response.put("timestamp", System.currentTimeMillis());
            
            LOGGER.info("Client started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Error starting client", e);
            metricsConfig.getFailedApiCalls().incrementAndGet();
            throw e;
        } finally {
            sample.stop(requestTimer);
            LoggingUtils.clearMdc();
        }
    }

    /**
     * Stop the data bridge.
     *
     * @return the response entity
     */
    @PostMapping("/stop")
    @Operation(summary = "Stop client", description = "Stops the data bridge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client stopped successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> stopClient() {
        String correlationId = LoggingUtils.setupMdc("stop_client");
        Timer.Sample sample = Timer.start();
        
        try {
            LOGGER.info("Stopping client");
            metricsConfig.getApiRequestsReceived().incrementAndGet();
            
            dataBridge.stop();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "stopped");
            response.put("timestamp", System.currentTimeMillis());
            
            LOGGER.info("Client stopped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Error stopping client", e);
            metricsConfig.getFailedApiCalls().incrementAndGet();
            throw e;
        } finally {
            sample.stop(requestTimer);
            LoggingUtils.clearMdc();
        }
    }

    /**
     * Restart the data bridge.
     *
     * @return the response entity
     */
    @PostMapping("/restart")
    @Operation(summary = "Restart client", description = "Restarts the data bridge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client restarted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> restartClient() {
        String correlationId = LoggingUtils.setupMdc("restart_client");
        Timer.Sample sample = Timer.start();
        
        try {
            LOGGER.info("Restarting client");
            metricsConfig.getApiRequestsReceived().incrementAndGet();
            
            dataBridge.restart();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "restarted");
            response.put("timestamp", System.currentTimeMillis());
            
            LOGGER.info("Client restarted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Error restarting client", e);
            metricsConfig.getFailedApiCalls().incrementAndGet();
            throw e;
        } finally {
            sample.stop(requestTimer);
            LoggingUtils.clearMdc();
        }
    }
    
    /**
     * Get metrics data for the client.
     *
     * @return the metrics data
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get client metrics", description = "Returns the current metrics for the client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> getMetrics() {
        String correlationId = LoggingUtils.setupMdc("get_client_metrics");
        Timer.Sample sample = Timer.start();
        
        try {
            LOGGER.info("Getting client metrics");
            metricsConfig.getApiRequestsReceived().incrementAndGet();
            
            Map<String, Object> metrics = getMetricsData();
            
            LOGGER.info("Client metrics retrieved");
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            LOGGER.error("Error getting client metrics", e);
            metricsConfig.getFailedApiCalls().incrementAndGet();
            throw e;
        } finally {
            sample.stop(requestTimer);
            LoggingUtils.clearMdc();
        }
    }
    
    /**
     * Get metrics data.
     *
     * @return the metrics data
     */
    private Map<String, Object> getMetricsData() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("dataPointsProcessed", metricsConfig.getDataPointsProcessed().get());
        metrics.put("dataPointsPublished", metricsConfig.getDataPointsPublished().get());
        metrics.put("apiRequestsReceived", metricsConfig.getApiRequestsReceived().get());
        metrics.put("activeDataStreams", metricsConfig.getActiveDataStreams().get());
        metrics.put("failedApiCalls", metricsConfig.getFailedApiCalls().get());
        return metrics;
    }
}
