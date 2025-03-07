package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.service.LaunchPool;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;

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
        final var days = getDays(args);
        sendMessage(chatId, "Fetching launch pool information for " + days + " day(s)...", client);
        launchPool.getLaunchPools(chatId, days)
                .thenAccept(launchPools -> Arrays.stream(launchPools)
                        .forEach(launchPool -> sendMessage(chatId, launchPool, client)));
    }

    private int getDays(final String args) {
        var days = 14;
        if (!args.isEmpty()) {
            try {
                days = Integer.parseInt(args);
                if (days < 1) {
                    days = 14;
                }
            } catch (NumberFormatException _) {
            }
        }

        return days;
    }
}
