package com.github.akarazhev.cryptoscout;

import java.util.List;

public record Event(Platform platform,
                    long time,
                    long publishTime,
                    long startTime,
                    long endTime,
                    String title,
                    String description,
                    String type,
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
                ", time=" + time +
                ", publishTime=" + publishTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + tags +
                ", url='" + url + '\'' +
                '}';
    }
}
