package com.github.akarazhev.cryptoscout.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
final class LaunchPoolService implements LaunchPool {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;

    public LaunchPoolService(final AmqpTemplate amqpTemplate,
                             @Value("${amqp.exchange.messages}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @Async
    @Override
    public CompletableFuture<String[]> getLaunchPools(final long chatId, final int days) {
        // Simulate a delay that might occur with a real API call
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // In a real application, you would fetch actual launch pool data from an API
        final var launchPool = """
                Launch pool info for a token :
                Start Date: 2025-01-01
                Duration: 30 days
                Staking Token: BNB
                Total Reward: 500,000
                Platform: Binance Launch Pool""";
        return CompletableFuture.completedFuture(new String[]{launchPool});
    }
}