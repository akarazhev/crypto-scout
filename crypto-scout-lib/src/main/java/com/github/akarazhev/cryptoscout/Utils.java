package com.github.akarazhev.cryptoscout;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static class Arguments {
        private Arguments() {
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

    public static class Formatter {
        private Formatter() {
            throw new UnsupportedOperationException();
        }

        public static String format(final long eventTime) {
            // You'll need to implement this method to format the eventTime as needed
            // For example, converting the long to a readable date-time string
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(eventTime));
        }
    }
}
