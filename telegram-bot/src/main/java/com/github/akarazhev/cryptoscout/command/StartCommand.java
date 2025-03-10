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

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
final class StartCommand extends InvokeCommand {

    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        String welcome = "Welcome to CryptoScout! 🚀\n\n" +
                "I'm here to help you find crypto opportunities and provide useful information about the cryptocurrency market.\n\n" +
                "Here's what I can do for you:\n" +
                "• Find potential crypto investment opportunities\n" +
                "• Provide real-time market data and price alerts\n" +
                "• Offer insights on trending cryptocurrencies\n" +
                "• Share news and updates from the crypto world\n\n" +
                "To get started, try some of our commands or simply ask me for /help!";
        sendMessage(chatId, welcome, client);
    }
}
