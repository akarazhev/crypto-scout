package com.github.akarazhev.cryptoscout.collector;

import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.Map;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.CancelCallback;

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
                this.connection = createConnection();
                this.channel = connection.createChannel();
                this.channel.basicQos(64); // backpressure
                declareQueuesIfNeeded();

                final DeliverCallback cmcCallback = (tag, delivery) ->
                        handleMessage(delivery.getBody(), QueueType.CMC, delivery.getEnvelope().getDeliveryTag());
                final DeliverCallback bybitCallback = (tag, delivery) ->
                        handleMessage(delivery.getBody(), QueueType.BYBIT, delivery.getEnvelope().getDeliveryTag());
                final DeliverCallback bybitStreamCallback = (tag, delivery) ->
                        handleMessage(delivery.getBody(), QueueType.BYBIT_STREAM, delivery.getEnvelope().getDeliveryTag());
                final CancelCallback cancelCallback = tag -> LOGGER.warn("Consumer canceled: {}", tag);

                cmcConsumerTag = channel.basicConsume(AmqpConfig.getAmqpQueueCmc(), false, cmcCallback,
                        cancelCallback);
                bybitConsumerTag = channel.basicConsume(AmqpConfig.getAmqpQueueBybit(), false, bybitCallback,
                        cancelCallback);
                bybitStreamConsumerTag = channel.basicConsume(AmqpConfig.getAmqpStreamBybit(), false, bybitStreamCallback,
                        cancelCallback);

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
            tryCancelConsumers();
            closeChannel();
            closeConnection();
            LOGGER.info("AmqpConsumer stopped");
        });
    }

    private enum QueueType {CMC, BYBIT, BYBIT_STREAM}

    private void handleMessage(final byte[] body, final QueueType type, final long deliveryTag) {
        // Deserialize and process off the reactor thread
        Promise.ofBlocking(executor, () -> {
            try {
                final var payload = JsonUtils.bytes2Object(body, Payload.class);
                switch (type) {
                    case CMC -> cmcService.save(payload);
                    case BYBIT, BYBIT_STREAM -> bybitService.save(payload);
                }

                ack(deliveryTag);
            } catch (final Exception ex) {
                LOGGER.error("Failed to process message from {}: {}", type, ex.getMessage(), ex);
                nack(deliveryTag);
            }

            return null;
        });
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
        // Ensure queues exist (publisher also declares them, this is idempotent)
        final var args = (Map<String, Object>) null;
        channel.queueDeclare(AmqpConfig.getAmqpQueueCmc(), true, false, false, args);
        channel.queueDeclare(AmqpConfig.getAmqpQueueBybit(), true, false, false, args);
        channel.queueDeclare(AmqpConfig.getAmqpStreamBybit(), true, false, false, args);
    }

    private Connection createConnection() throws IOException, TimeoutException {
        final var factory = new ConnectionFactory();
        factory.setHost(AmqpConfig.getAmqpRabbitmqHost());
        factory.setPort(AmqpConfig.getAmqpRabbitmqPort());
        factory.setUsername(AmqpConfig.getAmqpRabbitmqUsername());
        factory.setPassword(AmqpConfig.getAmqpRabbitmqPassword());
        return factory.newConnection("collector-consumer");
    }

    private void tryCancelConsumers() {
        try {
            if (channel != null && channel.isOpen() && cmcConsumerTag != null) {
                channel.basicCancel(cmcConsumerTag);
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error canceling CMC consumer", ex);
        }

        try {
            if (channel != null && channel.isOpen() && bybitConsumerTag != null) {
                channel.basicCancel(bybitConsumerTag);
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error canceling BYBIT consumer", ex);
        }

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
