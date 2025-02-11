package com.github.akarazhev.cryptoscout.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AmqpConfig {

    @Bean
    public TopicExchange announcementsTopicExchange(@Value("${amqp.exchange.announcements}") final String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
