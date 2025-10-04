package com.github.akarazhev.cryptoscout.collector;

import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;

public final class AmqpConsumer extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);
    private final Executor executor;
    private final BybitService bybitService;
    private final CmcService cmcService;
    private volatile Environment environment;
    private volatile Consumer metricsCmcConsumer;
    private volatile Consumer metricsBybitConsumer;
    private volatile Consumer streamBybitConsumer;

    private enum StreamType {CMC, BYBIT, BYBIT_STREAM}

    public static AmqpConsumer create(final NioReactor reactor, final Executor executor, final BybitService bybitService,
                                      final CmcService cmcService) {
        return new AmqpConsumer(reactor, executor, bybitService, cmcService);
    }

    private AmqpConsumer(final NioReactor reactor, final Executor executor, final BybitService bybitService,
                         final CmcService cmcService) {
        super(reactor);
        this.executor = executor;
        this.bybitService = bybitService;
        this.cmcService = cmcService;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
            try {
                LOGGER.info("Starting AmqpConsumer...");
                environment = AmqpConfig.getEnvironment();
                metricsCmcConsumer = environment.consumerBuilder()
                        .name(AmqpConfig.getAmqpStreamMetricsCmc())
                        .stream(AmqpConfig.getAmqpStreamMetricsCmc())
                        .offset(OffsetSpecification.first())
                        .manualTrackingStrategy()
                        .builder()
                        .messageHandler((context, message) ->
                                handleMessage(message.getBodyAsBinary(), StreamType.CMC, context::storeOffset))
                        .build();
                metricsBybitConsumer = environment.consumerBuilder()
                        .name(AmqpConfig.getAmqpStreamMetricsBybit())
                        .stream(AmqpConfig.getAmqpStreamMetricsBybit())
                        .offset(OffsetSpecification.first())
                        .manualTrackingStrategy()
                        .builder()
                        .messageHandler((context, message) ->
                                handleMessage(message.getBodyAsBinary(), StreamType.BYBIT, context::storeOffset))
                        .build();
                streamBybitConsumer = environment.consumerBuilder()
                        .name(AmqpConfig.getAmqpStreamCryptoBybit())
                        .stream(AmqpConfig.getAmqpStreamCryptoBybit())
                        .offset(OffsetSpecification.first())
                        .manualTrackingStrategy()
                        .builder()
                        .messageHandler((context, message) ->
                                handleMessage(message.getBodyAsBinary(), StreamType.BYBIT_STREAM, context::storeOffset))
                        .build();
                LOGGER.info("AmqpConsumer started");
            } catch (final Exception ex) {
                LOGGER.error("Failed to start AmqpConsumer", ex);
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
            LOGGER.info("Stopping AmqpConsumer...");
            closeConsumer(metricsCmcConsumer);
            metricsCmcConsumer = null;
            closeConsumer(metricsBybitConsumer);
            metricsBybitConsumer = null;
            closeConsumer(streamBybitConsumer);
            streamBybitConsumer = null;
            closeEnvironment();
            LOGGER.info("AmqpConsumer stopped");
        });
    }

    private void handleMessage(final byte[] body, final StreamType type, final Runnable storeOffset) {
        // Ensure we initiate the Promise on the reactor thread; stream callbacks run on a different thread
        LOGGER.info("Received payload: {}", new String(body));
        reactor.execute(() ->
                Promise.ofBlocking(executor, () -> JsonUtils.bytes2Object(body, Payload.class))
                        .then(payload -> switch (type) {
                            case CMC -> cmcService.save(payload);
                            case BYBIT, BYBIT_STREAM -> bybitService.save(payload);
                        })
                        .whenComplete(($, ex) -> {
                            if (ex == null) {
                                try {
                                    storeOffset.run();
                                } catch (final Exception e) {
                                    LOGGER.warn("Failed to store offset: {}", e.getMessage(), e);
                                }
                            } else {
                                LOGGER.error("Failed to process stream message from {}", type, ex);
                            }
                        })
        );
    }

    private void closeConsumer(final Consumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing stream consumer", ex);
        }
    }

    private void closeEnvironment() {
        try {
            if (environment != null) {
                environment.close();
                environment = null;
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing stream environment", ex);
        }
    }
}
