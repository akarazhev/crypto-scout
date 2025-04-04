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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
final class CommandHandler implements Commander {
    private final static Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final Map<String, Command> commands;

    public CommandHandler(final List<Command> commandList) {
        commands = new HashMap<>();
        for (final var command : commandList) {
            commands.put(command.getName(), command);
        }
    }

    @Override
    public void execute(final String message, final long chatId, final TelegramClient client) {
        final var parts = message.split("\\s+", 2);
        final var commandName = parts[0].toLowerCase();
        final var args = parts.length > 1 ? parts[1] : "";
        final var command = commands.get(commandName);
        if (command != null) {
            command.execute(chatId, args, client);
        } else {
            sendMessage(chatId, "Unknown command. Type /help for available commands.", client);
        }
    }

    private void sendMessage(final long chatId, final String text, final TelegramClient client) {
        final var message = SendMessage
                .builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            client.execute(message);
        } catch (final TelegramApiException e) {
            LOGGER.error("Error sending message", e);
        }
    }
}
