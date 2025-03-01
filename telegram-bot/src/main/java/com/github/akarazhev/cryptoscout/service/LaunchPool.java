package com.github.akarazhev.cryptoscout.service;

import java.util.concurrent.CompletableFuture;

public interface LaunchPool {

    CompletableFuture<String> getLaunchPoolInfo(final int days);
}
