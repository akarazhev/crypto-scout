package com.github.akarazhev.cryptoscout.consumer;

import com.github.akarazhev.cryptoscout.config.AmqpConfig;
import com.github.akarazhev.jcryptolib.util.JsonUtils;
import io.activej.async.service.ReactiveService;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.nio.NioReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP;

import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.CONNECTION_NAME;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.CONTENT_TYPE;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.DELIVERY_MODE;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.ROUTING_KEY_CLIENT;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.ROUTING_KEY_COLLECTOR;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.ROUTING_KEY_CRYPTO_BYBIT;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.ROUTING_KEY_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.ROUTING_KEY_METRICS_CMC;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.STREAM;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.TOPIC;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_DEAD_LETTER_EXCHANGE;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_DEAD_LETTER_EXCHANGE_VALUE;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_DEAD_LETTER_ROUTING_KEY;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_MAX_LENGTH;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_MAX_LENGTH_BYTES;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_MESSAGE_TTL;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_QUEUE_TYPE;
import static com.github.akarazhev.cryptoscout.consumer.Constants.AMQP.X_STREAM_MAX_SEGMENT_SIZE_BYTES;

public final class AmqpClient extends AbstractReactive implements ReactiveService, Publisher<Payload<Map<String, Object>>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpClient.class);
    private final Executor executor;
    private volatile Connection connection;
    private volatile Channel channel;

    public static AmqpClient create(final NioReactor reactor, final Executor executor) {
        return new AmqpClient(reactor, executor);
    }

    private AmqpClient(final NioReactor reactor, final Executor executor) {
        super(reactor);
        this.executor = executor;
    }

    @Override
    public Promise<?> start() {
        return Promise.ofBlocking(executor, () -> {
            try {
                LOGGER.info("Starting AmqpClient...");
                this.connection = createConnection();
                this.channel = connection.createChannel();
                // Enable publisher confirms for reliability
                this.channel.confirmSelect();
                declareExchanges();
                declareQueuesAndBindings();
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

    @Override
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

            final byte[] body = JsonUtils.object2Bytes(payload);
            final AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
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

    private void declareExchanges() throws Exception {
        // durable topic exchanges
        channel.exchangeDeclare(AmqpConfig.getAmqpExchangeMetrics(), TOPIC, true);
        channel.exchangeDeclare(AmqpConfig.getAmqpExchangeCrypto(), TOPIC, true);
        channel.exchangeDeclare(AmqpConfig.getAmqpExchangeClient(), TOPIC, true);
        channel.exchangeDeclare(AmqpConfig.getAmqpExchangeCollector(), TOPIC, true);
    }

    private void declareQueuesAndBindings() throws Exception {
        // Dead-lettered metric queues
        final var dlArgs = new HashMap<String, Object>();
        dlArgs.put(X_DEAD_LETTER_EXCHANGE, X_DEAD_LETTER_EXCHANGE_VALUE);
        dlArgs.put(X_DEAD_LETTER_ROUTING_KEY, AmqpConfig.getAmqpQueueDead());
        dlArgs.put(X_MESSAGE_TTL, AmqpConfig.getAmqpQueueTtlMs());
        dlArgs.put(X_MAX_LENGTH, AmqpConfig.getAmqpQueueMaxLength());

        channel.queueDeclare(AmqpConfig.getAmqpQueueCmc(), true, false, false, dlArgs);
        channel.queueDeclare(AmqpConfig.getAmqpQueueBybit(), true, false, false, dlArgs);
        // Dead-letter queue itself
        channel.queueDeclare(AmqpConfig.getAmqpQueueDead(), true, false, false, null);
        // Client and collector queues
        final var ccArgs = new HashMap<String, Object>();
        ccArgs.put(X_MESSAGE_TTL, AmqpConfig.getAmqpQueueTtlMs());
        ccArgs.put(X_MAX_LENGTH, AmqpConfig.getAmqpQueueMaxLength());

        channel.queueDeclare(AmqpConfig.getAmqpQueueClient(), true, false, false, ccArgs);
        channel.queueDeclare(AmqpConfig.getAmqpQueueCollector(), true, false, false, ccArgs);
        // Stream queue for crypto bybit
        final var streamArgs = new HashMap<String, Object>();
        streamArgs.put(X_QUEUE_TYPE, STREAM);
        streamArgs.put(X_MAX_LENGTH_BYTES, AmqpConfig.getAmqpStreamMaxBytes());
        streamArgs.put(X_STREAM_MAX_SEGMENT_SIZE_BYTES, AmqpConfig.getAmqpStreamSegmentBytes());
        channel.queueDeclare(AmqpConfig.getAmqpStreamBybit(), true, false, false, streamArgs);
        // Bindings
        channel.queueBind(AmqpConfig.getAmqpQueueCmc(), AmqpConfig.getAmqpExchangeMetrics(), ROUTING_KEY_METRICS_CMC);
        channel.queueBind(AmqpConfig.getAmqpQueueBybit(), AmqpConfig.getAmqpExchangeMetrics(), ROUTING_KEY_METRICS_BYBIT);
        channel.queueBind(AmqpConfig.getAmqpStreamBybit(), AmqpConfig.getAmqpExchangeCrypto(), ROUTING_KEY_CRYPTO_BYBIT);
        channel.queueBind(AmqpConfig.getAmqpQueueClient(), AmqpConfig.getAmqpExchangeClient(), ROUTING_KEY_CLIENT);
        channel.queueBind(AmqpConfig.getAmqpQueueCollector(), AmqpConfig.getAmqpExchangeCollector(), ROUTING_KEY_COLLECTOR);
    }

    private Connection createConnection() throws IOException, TimeoutException {
        final var factory = new ConnectionFactory();
        factory.setHost(AmqpConfig.getSpringRabbitmqHost());
        factory.setPort(AmqpConfig.getSpringRabbitmqPort());
        factory.setUsername(AmqpConfig.getSpringRabbitmqUsername());
        factory.setPassword(AmqpConfig.getSpringRabbitmqPassword());
        return factory.newConnection(CONNECTION_NAME);
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
