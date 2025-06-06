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
final class ByVotesCommand extends InvokeCommand {
    private final CryptoScout cryptoScout;
    private static final Logger LOGGER = LoggerFactory.getLogger(ByVotesCommand.class);

    public ByVotesCommand(final CryptoScout cryptoScout) {
        this.cryptoScout = cryptoScout;
    }

    @Override
    public String getName() {
        return "/byvotes";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        int days = asDays(args);
        cryptoScout.getByVotes(chatId, days)
                .thenAccept(votes -> {
                    if (votes.isEmpty()) {
                        sendMessage(chatId, "No by votes found for the last " + days + " days.", client);
                    } else {
                        votes.forEach(vote -> sendMessage(chatId, vote, client));
                    }
                })
                .exceptionally(ex -> {
                    sendMessage(chatId, "Error fetching by votes: " + ex.getMessage(), client);
                    LOGGER.error("Error fetching by votes", ex);
                    return null;
                });
    }
}
