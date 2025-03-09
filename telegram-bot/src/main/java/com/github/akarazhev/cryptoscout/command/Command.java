package com.github.akarazhev.cryptoscout.command;

import org.telegram.telegrambots.meta.generics.TelegramClient;

interface Command {

    String getName();

    void execute(final long chatId, final String args, final TelegramClient client);
}
