package com.github.akarazhev.cryptoscout.service;

import java.util.concurrent.CompletableFuture;

public interface LaunchPad {

    CompletableFuture<String> getLaunchPadInfo(final int days);
}
