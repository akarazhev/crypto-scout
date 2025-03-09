package com.github.akarazhev.cryptoscout.command;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface Commander {

    void execute(final String message, final long chatId, final TelegramClient client);
}
