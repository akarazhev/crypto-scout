package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
final class LongPollingBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(LongPollingBot.class);
    private final TelegramClient client;
    private final String token;
    private final CommandHandler commandHandler;

    public LongPollingBot(@Value("${telegram.bot.token}") final String token, final CommandHandler commandHandler) {
        this.token = token;
        this.client = new OkHttpTelegramClient(getBotToken());
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final var messageText = update.getMessage().getText();
            final var chatId = update.getMessage().getChatId();
            commandHandler.handle(messageText, chatId, client);
        }
    }

    @AfterBotRegistration
    public void afterRegistration(final BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}
