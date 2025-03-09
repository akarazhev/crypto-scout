package com.github.akarazhev.cryptoscout;

public record Message<T>(long chatId, Action action, T data) {
    public enum Action {
        LAUNCH_POOL, LAUNCH_PAD;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
