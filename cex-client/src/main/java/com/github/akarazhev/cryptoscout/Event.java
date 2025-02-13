package com.github.akarazhev.cryptoscout;

import java.util.List;

public record Event(Platform platform,
                    long eventTime,
                    String title,
                    String description,
                    String eventType,
                    List<String> tags,
                    String url
) {
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
                ", eventTime=" + eventTime +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", eventType='" + eventType + '\'' +
                ", tags=" + tags +
                ", url='" + url + '\'' +
                '}';
    }
}
