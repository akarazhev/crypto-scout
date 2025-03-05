package com.github.akarazhev.cryptoscout;

public record Message(long chatId, Action action, Object[] data) {
    public enum Action {
        LAUNCH_POOL, LAUNCH_PAD;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
