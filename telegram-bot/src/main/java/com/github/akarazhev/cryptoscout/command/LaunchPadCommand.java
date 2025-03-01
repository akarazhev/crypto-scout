package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.service.LaunchPad;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

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
        if (args.isEmpty()) {
            sendMessage(chatId, "Please provide a period in days.", client);
            return;
        }

        sendMessage(chatId, "Fetching launch pad information for " + args + " day(s)...", client);
        final var launchpadFuture = launchPad.getLaunchPadInfo(Integer.parseInt(args));
        launchpadFuture.thenAccept(info -> sendMessage(chatId, info, client));
    }
}
