package com.github.akarazhev.cryptoscout.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class InvokeCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(InvokeCommand.class);

    protected void sendMessage(final long chatId, final String text, final TelegramClient client) {
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
