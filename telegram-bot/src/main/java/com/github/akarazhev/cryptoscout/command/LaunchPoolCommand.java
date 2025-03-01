package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.service.LaunchPool;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
final class LaunchPoolCommand extends InvokeCommand {
    private final LaunchPool launchPool;

    public LaunchPoolCommand(final LaunchPool launchPool) {
        this.launchPool = launchPool;
    }

    @Override
    public String getCommandName() {
        return "/launchpool";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        if (args.isEmpty()) {
            sendMessage(chatId, "Please provide a period in days.", client);
            return;
        }

        sendMessage(chatId, "Fetching launch pool information for " + args + " day(s)...", client);
        final var launchPoolFuture = launchPool.getLaunchPoolInfo(Integer.parseInt(args));
        launchPoolFuture.thenAccept(info -> sendMessage(chatId, info, client));
    }
}
