package com.github.akarazhev.cryptoscout;

import java.util.concurrent.CompletableFuture;

public interface CryptoScout {

    CompletableFuture<String> getLaunchPads(final long chatId, final int days);

    CompletableFuture<String> getLaunchPools(final long chatId, final int days);

    void subscribe(final Message message);
}
