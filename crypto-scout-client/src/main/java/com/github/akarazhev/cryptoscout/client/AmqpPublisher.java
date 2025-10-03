package com.github.akarazhev.cryptoscout.client;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
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

import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.CONTENT_TYPE;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.DELIVERY_MODE;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.ROUTING_KEY_CRYPTO_BYBIT;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.ROUTING_KEY_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.client.Constants.AMQP.ROUTING_KEY_METRICS_CMC;

public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpPublisher.class);
    private final Executor executor;
    private volatile Connection connection;
    private volatile Channel channel;

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
                LOGGER.info("Starting AmqpClient...");
                this.connection = AmqpConfig.getConnection();
                this.channel = connection.createChannel();
                // Enable publisher confirms for reliability
                this.channel.confirmSelect();
                LOGGER.info("AmqpClient started and topology declared");
            } catch (final Exception ex) {
                LOGGER.error("Failed to start AmqpClient", ex);
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Promise<?> stop() {
        return Promise.ofBlocking(executor, () -> {
            LOGGER.info("Stopping AmqpClient...");
            closeChannel();
            closeConnection();
            LOGGER.info("AmqpClient stopped");
        });
    }

    public Promise<?> publish(final Payload<Map<String, Object>> payload) {
        // offload blocking publish to executor, keep reactor non-blocking
        return Promise.ofBlocking(executor, () -> {
            String exchange = null;
            String routingKey = null;
            final var provider = payload.getProvider();
            final var source = payload.getSource();
            if (Provider.CMC.equals(provider)) {
                if (Source.FGI.equals(source)) {
                    exchange = AmqpConfig.getAmqpExchangeMetrics();
                    routingKey = ROUTING_KEY_METRICS_CMC;
                }
            } else if (Provider.BYBIT.equals(provider)) {
                if (Source.LPL.equals(source)) {
                    exchange = AmqpConfig.getAmqpExchangeMetrics();
                    routingKey = ROUTING_KEY_METRICS_BYBIT;
                } else if (Source.PMST.equals(source)) {
                    exchange = AmqpConfig.getAmqpExchangeCrypto();
                    routingKey = ROUTING_KEY_CRYPTO_BYBIT;
                }
            }

            if (exchange == null || routingKey == null) {
                LOGGER.debug("Skipping publish: no route for provider={} source={}", provider, source);
                return null;
            }

            final var body = JsonUtils.object2Bytes(payload);
            final var props = new AMQP.BasicProperties.Builder()
                    .contentType(CONTENT_TYPE)
                    .deliveryMode(DELIVERY_MODE) // persistent
                    .build();

            synchronized (this) {
                if (channel == null || !channel.isOpen()) {
                    throw new IllegalStateException("AMQP channel is not open");
                }

                channel.basicPublish(exchange, routingKey, props, body);
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
}
