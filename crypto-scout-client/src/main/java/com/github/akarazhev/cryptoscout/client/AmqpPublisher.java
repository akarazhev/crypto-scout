package com.github.akarazhev.cryptoscout.client;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.promise.SettablePromise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.Map;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;

import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.CONNECTION_NAME;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.CONTENT_TYPE;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.DELIVERY_MODE;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.ROUTING_KEY_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.ROUTING_KEY_METRICS_CMC;

public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpPublisher.class);
    private final Executor executor;
    private volatile Connection connection;
    private volatile Channel channel;
    private volatile Environment environment;
    private volatile Producer producer;

    public static AmqpPublisher create(final NioReactor reactor, final Executor executor) {
        return new AmqpPublisher(reactor, executor);
    }

    private AmqpPublisher(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
            try {
                LOGGER.info("Starting AmqpPublisher...");
                connection = AmqpConfig.getConnection();
                channel = connection.createChannel();
                // Enable publisher confirms for reliability
                channel.confirmSelect();

                environment = AmqpConfig.getEnvironment();
                final var stream = AmqpConfig.getAmqpStreamBybit();
                producer = environment.producerBuilder()
                        .name(CONNECTION_NAME)
                        .stream(stream)
                        .build();
                LOGGER.info("AmqpPublisher started");
            } catch (final Exception ex) {
                LOGGER.error("Failed to start AmqpPublisher", ex);
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
            LOGGER.info("Stopping AmqpPublisher...");
            closeProducer();
            closeEnvironment();
            closeChannel();
            closeConnection();
            LOGGER.info("AmqpPublisher stopped");
        });
    }

    public Promise<?> publish(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        // Stream path: BYBIT + PMST (async confirm via ActiveJ Promise)
        if (Provider.BYBIT.equals(provider) && Source.PMST.equals(source)) {
            final var settablePromise = new SettablePromise<Void>();
            try {
                if (producer == null) {
                    throw new IllegalStateException("Stream producer is not initialized");
                }

                final var message = producer.messageBuilder()
                        .addData(JsonUtils.object2Bytes(payload))
                        .build();
                producer.send(message, confirmationStatus ->
                        reactor.execute(() -> {
                            if (confirmationStatus.isConfirmed()) {
                                settablePromise.set(null);
                            } else {
                                settablePromise.setException(new RuntimeException("Stream publish not confirmed: " +
                                        confirmationStatus));
                            }
                        })
                );
            } catch (final Exception ex) {
                LOGGER.error("Failed to publish payload to stream: {}", ex.getMessage(), ex);
                settablePromise.setException(ex);
            }

            return settablePromise;
        }
        // AMQP path: offload blocking publish to executor
        return Promise.ofBlocking(executor, () -> {
            String exchange = null;
            String routingKey = null;
            if (Provider.CMC.equals(provider)) {
                if (Source.FGI.equals(source)) {
                    exchange = AmqpConfig.getAmqpExchangeMetrics();
                    routingKey = ROUTING_KEY_METRICS_CMC;
                }
            } else if (Provider.BYBIT.equals(provider)) {
                if (Source.LPL.equals(source)) {
                    exchange = AmqpConfig.getAmqpExchangeMetrics();
                    routingKey = ROUTING_KEY_METRICS_BYBIT;
                }
            }

            if (exchange == null || routingKey == null) {
                LOGGER.debug("Skipping publish: no route for provider={} source={}", provider, source);
                return null;
            }

            final var bytes = JsonUtils.object2Bytes(payload);
            final var props = new AMQP.BasicProperties.Builder()
                    .contentType(CONTENT_TYPE)
                    .deliveryMode(DELIVERY_MODE) // persistent
                    .build();

            synchronized (this) {
                if (channel == null || !channel.isOpen()) {
                    throw new IllegalStateException("AMQP channel is not open");
                }

                channel.basicPublish(exchange, routingKey, props, bytes);
                // wait for publish confirm for at-least-once delivery semantics
                channel.waitForConfirmsOrDie();
            }

            return null;
        }).whenException(ex -> LOGGER.error("Failed to publish payload: {}", ex.getMessage(), ex));
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

    private void closeProducer() {
        try {
            if (producer != null) {
                producer.close();
                producer = null;
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing RabbitMQ producer", ex);
        }
    }

    private void closeEnvironment() {
        try {
            if (environment != null) {
                environment.close();
                environment = null;
            }
        } catch (final Exception ex) {
            LOGGER.warn("Error closing RabbitMQ environment", ex);
        }
    }
}
