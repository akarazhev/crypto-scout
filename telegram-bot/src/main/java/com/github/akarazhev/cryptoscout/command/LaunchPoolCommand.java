package com.github.akarazhev.cryptoscout.command;

import com.github.akarazhev.cryptoscout.CryptoScout;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static com.github.akarazhev.cryptoscout.Utils.Arguments.asDays;

@Component
final class LaunchPoolCommand extends InvokeCommand {
    private final CryptoScout cryptoScout;

    public LaunchPoolCommand(final CryptoScout cryptoScout) {
        this.cryptoScout = cryptoScout;
    }

    @Override
    public String getName() {
        return "/launchpool";
    }

    @Override
    public void execute(final long chatId, final String args, final TelegramClient client) {
        cryptoScout.getLaunchPools(chatId, asDays(args))
                .thenAccept(launchPools -> sendMessage(chatId, launchPools, client));
    }
}
