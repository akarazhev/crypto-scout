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

package com.github.akarazhev.cryptoscout.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_BYBIT_LPL;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_METRICS_CMC_FGI;

@Configuration
class AmqpConfig {

    @Bean
    public TopicExchange metricsExchange(@Value("${amqp.exchange.metrics}") final String name) {
        return ExchangeBuilder.topicExchange(name)
                .durable(true)
                .build();
    }

    @Bean
    public Queue cmcFearGreedIndexQueue(@Value("${amqp.queue.cmc_fear_greed_index}") final String queueName,
                                        @Value("${amqp.queue.dead}") final String deadLetterQueue) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", deadLetterQueue)
                .ttl(21600000)
                .maxLength(2500)
                .build();
    }

    @Bean
    public Queue bybitLaunchPoolQueue(@Value("${amqp.queue.bybit_launch_pool}") final String queueName,
                                      @Value("${amqp.queue.dead}") final String deadLetterQueue) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", deadLetterQueue)
                .ttl(21600000)
                .maxLength(2500)
                .build();
    }

    @Bean
    public Queue clientQueue(@Value("${amqp.queue.client}") final String queueName) {
        return QueueBuilder.durable(queueName)
                .ttl((int) Duration.ofHours(6).toMillis())
                .maxLength(2500)
                .build();
    }

    @Bean
    public Queue deadLetterQueue(@Value("${amqp.queue.dead}") final String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding cmcFearGreedIndexBinding(final Queue cmcFearGreedIndexQueue, final TopicExchange metricsExchange) {
        return BindingBuilder.bind(cmcFearGreedIndexQueue)
                .to(metricsExchange)
                .with(ROUTING_METRICS_CMC_FGI);
    }

    @Bean
    public Binding bybitLaunchPoolBinding(final Queue bybitLaunchPoolQueue, final TopicExchange metricsExchange) {
        return BindingBuilder.bind(bybitLaunchPoolQueue)
                .to(metricsExchange)
                .with(ROUTING_METRICS_BYBIT_LPL);
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
