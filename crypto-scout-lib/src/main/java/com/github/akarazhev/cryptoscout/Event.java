package com.github.akarazhev.cryptoscout;

import java.util.Objects;

public record Event(Platform platform, long eventTime, String type, String title, String description, String url) {
    public enum Platform {
        BYBIT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var event = (Event) o;
        return eventTime == event.eventTime &&
                Objects.equals(url, event.url) &&
                Objects.equals(type, event.type) &&
                Objects.equals(title, event.title) &&
                platform == event.platform &&
                Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, eventTime, type, title, description, url);
    }

    @Override
    public String toString() {
        return "Event{" +
                "platform=" + platform +
                ", eventTime=" + eventTime +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
