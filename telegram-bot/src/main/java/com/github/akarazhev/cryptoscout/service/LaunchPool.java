package com.github.akarazhev.cryptoscout.service;

import com.github.akarazhev.cryptoscout.Message;

import java.util.concurrent.CompletableFuture;

public interface LaunchPool {

    void subscribe(final Message message);

    CompletableFuture<String[]> getLaunchPools(final long chatId, final int days);
}
