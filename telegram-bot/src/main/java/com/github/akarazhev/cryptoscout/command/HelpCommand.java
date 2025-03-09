package com.github.akarazhev.cryptoscout.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
final class HelpCommand extends InvokeCommand {

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        final var help = """
                Available commands:
                /launchpad [days] - Get information about a launch pad for days. Default value is 14.
                /launchpool [days] - Get information about a launch pool for days. Default value is 14.
                /help - Show this help message.
                """;
        sendMessage(chatId, help, client);
    }
}
