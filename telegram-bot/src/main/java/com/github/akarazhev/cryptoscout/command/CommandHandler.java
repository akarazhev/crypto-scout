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
