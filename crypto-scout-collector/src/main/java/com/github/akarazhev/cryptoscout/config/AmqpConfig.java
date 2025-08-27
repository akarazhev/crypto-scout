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
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_ANNOUNCEMENTS;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_COMMANDS;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_KEY_CLIENT;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_KEY_CRYPTO_BYBIT;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_KEY_METRICS_BYBIT;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_KEY_METRICS_CMC;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.X_DEAD_LETTER_EXCHANGE;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.X_DEAD_LETTER_EXCHANGE_VALUE;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.X_DEAD_LETTER_ROUTING_KEY;

@Configuration
class AmqpConfig {

    @Bean
    @Qualifier("announcementsExchange")
    public TopicExchange announcementsTopicExchange(@Value("${amqp.exchange.announcements}") final String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    @Qualifier("commandsExchange")
    public TopicExchange commandsTopicExchange(@Value("${amqp.exchange.commands}") final String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    @Qualifier("resultsExchange")
    public TopicExchange resultsTopicExchange(@Value("${amqp.exchange.results}") final String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    @Qualifier("metricsExchange")
    public TopicExchange metricsExchange(@Value("${amqp.exchange.metrics}") final String name) {
        return ExchangeBuilder.topicExchange(name)
                .durable(true)
                .build();
    }

    @Bean
    @Qualifier("cryptoExchange")
    public TopicExchange cryptoExchange(@Value("${amqp.exchange.crypto}") final String name) {
        return ExchangeBuilder.topicExchange(name)
                .durable(true)
                .build();
    }

    @Bean
    @Qualifier("clientExchange")
    public TopicExchange clientExchange(@Value("${amqp.exchange.client}") final String name) {
        return ExchangeBuilder.topicExchange(name)
                .durable(true)
                .build();
    }

    @Bean
    public Queue announcementsQueue(@Value("${amqp.queue.announcements}") final String queueName) {
        return QueueBuilder.durable(queueName).ttl((int) Duration.ofHours(6).toMillis())
                .maxLength(2500)
                .build();
    }

    @Bean
    public Binding announcementsBinding(final Queue announcementsQueue,
                                        @Qualifier("announcementsExchange") final TopicExchange announcementsExchange) {
        return BindingBuilder.bind(announcementsQueue)
                .to(announcementsExchange)
                .with(ROUTING_ANNOUNCEMENTS);
    }

    @Bean
    public Queue commandsQueue(@Value("${amqp.queue.events}") final String queueName) {
        return QueueBuilder.durable(queueName).ttl((int) Duration.ofHours(6).toMillis())
                .maxLength(2500)
                .build();
    }

    @Bean
    public Binding commandsBinding(final Queue commandsQueue,
                                   @Qualifier("commandsExchange") final TopicExchange commandsExchange) {
        return BindingBuilder.bind(commandsQueue)
                .to(commandsExchange)
                .with(ROUTING_COMMANDS);
    }

    @Bean
    public Queue cmcFearGreedIndexQueue(@Value("${amqp.queue.cmc}") final String queueName,
                                        @Value("${amqp.queue.dead}") final String deadLetterQueue,
                                        @Value("${amqp.queue.ttl.ms}") final int ttlMs,
                                        @Value("${amqp.queue.max.length}") final int maxLength) {
        return QueueBuilder.durable(queueName)
                .withArgument(X_DEAD_LETTER_EXCHANGE, X_DEAD_LETTER_EXCHANGE_VALUE)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, deadLetterQueue)
                .ttl(ttlMs)
                .maxLength(maxLength)
                .build();
    }

    @Bean
    public Queue bybitLaunchPoolQueue(@Value("${amqp.queue.bybit}") final String queueName,
                                      @Value("${amqp.queue.dead}") final String deadLetterQueue,
                                      @Value("${amqp.queue.ttl.ms}") final int ttlMs,
                                      @Value("${amqp.queue.max.length}") final int maxLength) {
        return QueueBuilder.durable(queueName)
                .withArgument(X_DEAD_LETTER_EXCHANGE, X_DEAD_LETTER_EXCHANGE_VALUE)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, deadLetterQueue)
                .ttl(ttlMs)
                .maxLength(maxLength)
                .build();
    }

    @Bean
    public Queue cryptoBybitQueue(@Value("${amqp.stream.crypto_bybit}") final String streamName) {
        return QueueBuilder.durable(streamName)
                .withArgument("x-queue-type", "stream")
                .withArgument("x-max-length-bytes", 2_000_000_000) // 2GB max size
                .withArgument("x-stream-max-segment-size-bytes", 100_000_000) // 100MB segments
                .build();
    }

    @Bean
    public Queue clientQueue(@Value("${amqp.queue.client}") final String queueName,
                             @Value("${amqp.queue.ttl.ms}") final int ttlMs,
                             @Value("${amqp.queue.max.length}") final int maxLength) {
        return QueueBuilder.durable(queueName)
                .ttl(ttlMs)
                .maxLength(maxLength)
                .build();
    }

    @Bean
    public Queue metricsDeadLetterQueue(@Value("${amqp.queue.dead}") final String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding cmcFearGreedIndexBinding(final Queue cmcFearGreedIndexQueue,
                                           @Qualifier("metricsExchange") final TopicExchange metricsExchange) {
        return BindingBuilder.bind(cmcFearGreedIndexQueue)
                .to(metricsExchange)
                .with(ROUTING_KEY_METRICS_CMC);
    }

    @Bean
    public Binding bybitLaunchPoolBinding(final Queue bybitLaunchPoolQueue,
                                         @Qualifier("metricsExchange") final TopicExchange metricsExchange) {
        return BindingBuilder.bind(bybitLaunchPoolQueue)
                .to(metricsExchange)
                .with(ROUTING_KEY_METRICS_BYBIT);
    }

    @Bean
    public Binding cryptoBybitBinding(final Queue cryptoBybitQueue,
                                     @Qualifier("cryptoExchange") final TopicExchange cryptoExchange) {
        return BindingBuilder.bind(cryptoBybitQueue)
                .to(cryptoExchange)
                .with(ROUTING_KEY_CRYPTO_BYBIT);
    }

    @Bean
    public Binding clientBinding(final Queue clientQueue,
                               @Qualifier("clientExchange") final TopicExchange clientExchange) {
        return BindingBuilder.bind(clientQueue)
                .to(clientExchange)
                .with(ROUTING_KEY_CLIENT);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
