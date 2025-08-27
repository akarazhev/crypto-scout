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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

import java.time.Duration;

import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_ANNOUNCEMENTS;
import static com.github.akarazhev.cryptoscout.Constants.AMQP.ROUTING_COMMANDS;

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
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        final var factory = new DefaultMessageHandlerMethodFactory();
        final var jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.getObjectMapper().registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        factory.setMessageConverter(jsonConverter);
        return factory;
    }

    @Bean
    public RabbitListenerConfigurer rabbitListenerConfigurer(final MessageHandlerMethodFactory messageHandlerMethodFactory) {
        return (c) -> c.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
