package com.github.akarazhev.cryptoscout.collector;

import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public final class AmqpConsumer extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);
    private final Executor executor;
    private final BybitService bybitService;
    private final CmcService cmcService;
    private volatile Connection connection;
    private volatile Channel channel;
    private volatile String cmcConsumerTag;
    private volatile String bybitConsumerTag;
    private volatile String bybitStreamConsumerTag;

    private enum QueueType {CMC, BYBIT, BYBIT_STREAM}

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
                this.connection = AmqpConfig.getConnection();
                this.channel = connection.createChannel();
                this.channel.basicQos(64); // backpressure
                declareQueuesIfNeeded();

                cmcConsumerTag = channel.basicConsume(AmqpConfig.getAmqpQueueCmc(), false,
                        (tag, delivery) ->
                                handleMessage(delivery.getBody(), QueueType.CMC, delivery.getEnvelope().getDeliveryTag()),
                        tag -> LOGGER.warn("Consumer canceled: {}", tag));
                bybitConsumerTag = channel.basicConsume(AmqpConfig.getAmqpQueueBybit(), false,
                        (tag, delivery) ->
                                handleMessage(delivery.getBody(), QueueType.BYBIT, delivery.getEnvelope().getDeliveryTag()),
                        tag -> LOGGER.warn("Consumer canceled: {}", tag));
                bybitStreamConsumerTag = channel.basicConsume(AmqpConfig.getAmqpStreamBybit(), false,
                        (tag, delivery) ->
                                handleMessage(delivery.getBody(), QueueType.BYBIT_STREAM, delivery.getEnvelope().getDeliveryTag()),
                        tag -> LOGGER.warn("Consumer canceled: {}", tag));

                LOGGER.info("AmqpConsumer started: consuming from queues [{}], [{}] and stream [{}]",
                        AmqpConfig.getAmqpQueueCmc(), AmqpConfig.getAmqpQueueBybit(), AmqpConfig.getAmqpStreamBybit());
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
            closeCmcConsumer();
            closeBybitConsumer();
            closeBybitStreamConsumer();
            closeChannel();
            closeConnection();
            LOGGER.info("AmqpConsumer stopped");
        });
    }

    private void handleMessage(final byte[] body, final QueueType type, final long deliveryTag) {
        // Ensure we initiate the Promise on the reactor thread; RabbitMQ callbacks run on a different thread
        reactor.execute(() ->
                Promise.ofBlocking(executor, () -> JsonUtils.bytes2Object(body, Payload.class))
                        .then(payload -> switch (type) {
                            case CMC -> cmcService.save(payload);
                            case BYBIT, BYBIT_STREAM -> bybitService.save(payload);
                        })
                        .whenComplete(($, ex) -> {
                            if (ex == null) {
                                ack(deliveryTag);
                            } else {
//                                LOGGER.error("Failed to process message from {}: {}", type, ex.getMessage(), ex);
                                LOGGER.error("Failed to process message from {}", type, ex);
                                nack(deliveryTag);
                            }
                        })
        );
    }

    private void ack(final long deliveryTag) {
        try {
            synchronized (this) {
                if (channel != null && channel.isOpen()) {
                    channel.basicAck(deliveryTag, false);
                }
            }
        } catch (final IOException ex) {
            LOGGER.warn("Ack failed: {}", ex.getMessage(), ex);
        }
    }

    private void nack(final long deliveryTag) {
        try {
            synchronized (this) {
                if (channel != null && channel.isOpen()) {
                    channel.basicNack(deliveryTag, false, false); // dead-letter
                }
            }
        } catch (final IOException ex) {
            LOGGER.warn("Nack failed: {}", ex.getMessage(), ex);
        }
    }

    private void declareQueuesIfNeeded() throws Exception {
        // Assert queues exist without redefining their arguments to avoid PRECONDITION_FAILED
        channel.queueDeclarePassive(AmqpConfig.getAmqpQueueCmc());
        channel.queueDeclarePassive(AmqpConfig.getAmqpQueueBybit());
        channel.queueDeclarePassive(AmqpConfig.getAmqpStreamBybit());
    }

    private void closeCmcConsumer() {
        try {
            if (channel != null && channel.isOpen() && cmcConsumerTag != null) {
                channel.basicCancel(cmcConsumerTag);
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error canceling CMC consumer", ex);
        }
    }

    private void closeBybitConsumer() {
        try {
            if (channel != null && channel.isOpen() && bybitConsumerTag != null) {
                channel.basicCancel(bybitConsumerTag);
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error canceling BYBIT consumer", ex);
        }
    }

    private void closeBybitStreamConsumer() {
        try {
            if (channel != null && channel.isOpen() && bybitStreamConsumerTag != null) {
                channel.basicCancel(bybitStreamConsumerTag);
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error canceling BYBIT stream consumer", ex);
        }
    }

    private void closeChannel() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing AMQP channel", ex);
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing AMQP connection", ex);
        }
    }
}
