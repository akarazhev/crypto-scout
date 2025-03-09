package com.github.akarazhev.cryptoscout;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static int asDays(final String args) {
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
