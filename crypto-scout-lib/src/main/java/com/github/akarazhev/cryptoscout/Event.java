package com.github.akarazhev.cryptoscout;

import com.github.akarazhev.cryptoscout.bybit.Announcement;

import java.util.Objects;

public record Event(Platform platform, String eventType, Announcement announcement) {
    public enum Platform {
        BYBIT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var event = (Event) o;
        return Objects.equals(eventType, event.eventType) &&
                platform == event.platform &&
                Objects.equals(announcement, event.announcement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, eventType, announcement);
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
