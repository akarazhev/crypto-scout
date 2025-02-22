package com.github.akarazhev.cryptoscout.bybit;

import java.util.List;

public record Announcement(String title, String description, Type type, List<String> tags, String url, long dateTimestamp) {

    @Override
    public String toString() {
        return '{' +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", tags=" + tags +
                ", url='" + url + '\'' +
                ", dateTimestamp=" + dateTimestamp +
                '}';
    }
}
