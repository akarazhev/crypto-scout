package com.github.akarazhev.cryptoscout.service;

import java.util.concurrent.CompletableFuture;

public interface LaunchPool extends MessageSubscriber {

    CompletableFuture<String[]> getLaunchPools(final long chatId, final int days);
}
