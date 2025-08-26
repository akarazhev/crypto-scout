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

import com.github.akarazhev.cryptoscout.Publisher;
import com.github.akarazhev.cryptoscout.config.MetricsConfig;
import com.github.akarazhev.cryptoscout.util.LoggingUtils;
import com.github.akarazhev.jcryptolib.stream.Payload;
import com.github.akarazhev.jcryptolib.stream.Provider;
import com.github.akarazhev.jcryptolib.stream.Source;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_BYBIT_LPL;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_CMC_FGI;

@Service
public final class DataPublisher implements Publisher<Payload<Map<String, Object>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPublisher.class);
    
    private final AmqpTemplate amqpTemplate;
    private final TopicExchange topicExchange;
    private final MetricsConfig metricsConfig;
    private final Timer publishTimer;

    public DataPublisher(final AmqpTemplate amqpTemplate, final TopicExchange topicExchange, 
                        final MetricsConfig metricsConfig, final MeterRegistry meterRegistry) {
        this.amqpTemplate = amqpTemplate;
        this.topicExchange = topicExchange;
        this.metricsConfig = metricsConfig;
        this.publishTimer = meterRegistry.timer("crypto.scout.data.publishing.duration");
    }

    @Override
    public void publish(final Payload<Map<String, Object>> payload) {
        LoggingUtils.withMdc(LOGGER, payload, "publish", () -> {
            final var provider = payload.getProvider();
            final var source = payload.getSource();
            final var data = payload.getData();
            
            Timer.Sample sample = Timer.start();
            
            try {
                if (Provider.CMC.equals(provider)) {
                    if (Source.FGI.equals(source)) {
                        LOGGER.info("Publishing CMC FGI data to exchange: {}, routing key: {}", 
                                topicExchange.getName(), ROUTING_METRICS_CMC_FGI);
                        amqpTemplate.convertAndSend(topicExchange.getName(), ROUTING_METRICS_CMC_FGI, data);
                        metricsConfig.getDataPointsPublished().incrementAndGet();
                    }
                } else if (Provider.BYBIT.equals(provider)) {
                    if (Source.LPL.equals(source)) {
                        LOGGER.info("Publishing Bybit LPL data to exchange: {}, routing key: {}", 
                                topicExchange.getName(), ROUTING_METRICS_BYBIT_LPL);
                        amqpTemplate.convertAndSend(topicExchange.getName(), ROUTING_METRICS_BYBIT_LPL, data);
                        metricsConfig.getDataPointsPublished().incrementAndGet();
                    }
                } else {
                    LOGGER.warn("Unknown provider/source combination: {}/{}", provider, source);
                }
            } catch (Exception e) {
                LOGGER.error("Error publishing data for provider: {}, source: {}", provider, source, e);
                throw e;
            } finally {
                sample.stop(publishTimer);
            }
        });
    }
}
