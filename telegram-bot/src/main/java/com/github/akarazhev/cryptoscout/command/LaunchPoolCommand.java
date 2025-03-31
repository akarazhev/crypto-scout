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

package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.CryptoScout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static com.github.akarazhev.cryptoscout.Utils.Arguments.asDays;

@Component
final class LaunchPoolCommand extends InvokeCommand {
    private final CryptoScout cryptoScout;
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchPoolCommand.class);

    public LaunchPoolCommand(final CryptoScout cryptoScout) {
        this.cryptoScout = cryptoScout;
    }

    @Override
    public String getName() {
        return "/launchpool";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        cryptoScout.getLaunchPools(chatId, asDays(args))
                .thenAccept(launchPools ->
                        launchPools.forEach(pool -> sendMessage(chatId, pool, client)))
                .exceptionally(ex -> {
                    sendMessage(chatId, "Error fetching launch pools: " + ex.getMessage(), client);
                    LOGGER.error("Error fetching launch pools", ex);
                    return null;
                });
    }
}
