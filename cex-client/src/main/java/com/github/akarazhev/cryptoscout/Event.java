package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.Announcement;

public record Event(Platform platform, String eventType, Announcement announcement) {
    public enum Platform {
        BYBIT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "platform=" + platform +
                ", eventType='" + eventType + '\'' +
                ", announcement=" + announcement +
                '}';
    }
}
