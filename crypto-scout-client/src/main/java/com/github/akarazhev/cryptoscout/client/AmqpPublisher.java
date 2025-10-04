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
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;

public final class AmqpPublisher extends AbstractReactive implements ReactiveService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AmqpPublisher.class);
    private final Executor executor;
    private volatile Environment environment;
    private volatile Producer streamBybitProducer;
    private volatile Producer metricsBybitProducer;
    private volatile Producer metricsCmcProducer;

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
                environment = AmqpConfig.getEnvironment();
                streamBybitProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpStreamBybit())
                        .stream(AmqpConfig.getAmqpStreamBybit())
                        .build();
                metricsBybitProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpStreamMetricsBybit())
                        .stream(AmqpConfig.getAmqpStreamMetricsBybit())
                        .build();
                metricsCmcProducer = environment.producerBuilder()
                        .name(AmqpConfig.getAmqpStreamMetricsCmc())
                        .stream(AmqpConfig.getAmqpStreamMetricsCmc())
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
            closeProducer(streamBybitProducer);
            streamBybitProducer = null;
            closeProducer(metricsBybitProducer);
            metricsBybitProducer = null;
            closeProducer(metricsCmcProducer);
            metricsCmcProducer = null;
            closeEnvironment();
            LOGGER.info("AmqpPublisher stopped");
        });
    }

    public Promise<?> publish(final Payload<Map<String, Object>> payload) {
        final var provider = payload.getProvider();
        final var source = payload.getSource();
        final var producer =
                Provider.CMC.equals(provider) && Source.FGI.equals(source) ? metricsCmcProducer :
                        Provider.BYBIT.equals(provider) && Source.LPL.equals(source) ? metricsBybitProducer :
                                Provider.BYBIT.equals(provider) && Source.PMST.equals(source) ? streamBybitProducer :
                                        null;

        if (producer == null) {
            LOGGER.debug("Skipping publish: no stream route for provider={} source={}", provider, source);
            return Promise.of(null);
        }

        final var settablePromise = new SettablePromise<Void>();
        try {
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

    private void closeProducer(final Producer producer) {
        try {
            if (producer != null) {
                producer.close();
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
