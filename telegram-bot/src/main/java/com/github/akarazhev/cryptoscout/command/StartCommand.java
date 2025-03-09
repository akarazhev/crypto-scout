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
