package com.github.akarazhev.cryptoscout.command;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface Command {

    String getCommandName();

    void execute(final long chatId, final String args, final TelegramClient client);
}
