package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.service.LaunchPad;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;

@Component
final class LaunchPadCommand extends InvokeCommand {
    private final LaunchPad launchPad;

    public LaunchPadCommand(final LaunchPad launchPad) {
        this.launchPad = launchPad;
    }

    @Override
    public String getCommandName() {
        return "/launchpad";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        var days = getDays(args);
        sendMessage(chatId, "Fetching launch pad information for " + days + " day(s)...", client);
        final var future = launchPad.getLaunchPads(chatId, days);
        future.thenAccept(launchPads -> Arrays.stream(launchPads)
                .forEach(launchPad -> sendMessage(chatId, launchPad, client)));
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
