package com.github.akarazhev.cryptoscout.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
final class LaunchPadService implements LaunchPad {
    private final AmqpTemplate amqpTemplate;
    private final String exchange;

    public LaunchPadService(final AmqpTemplate amqpTemplate,
                             @Value("${amqp.exchange.commands}") final String exchange) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
    }

    @Async
    @Override
    public CompletableFuture<String[]> getLaunchPads(final long chatId, final int days) {
        // Simulate a delay that might occur with a real API call
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // In a real application, you would fetch actual launchpad data from an API
        final var launchPad = """
                Launch pad info for a token :
                Start Date: 2025-01-01
                End Date: 2025-02-01
                Initial Price: $0.1
                Total Tokens: 1,000,000
                Platform: Binance Launch Pad""";
        return CompletableFuture.completedFuture(new String[]{launchPad});
    }
}
