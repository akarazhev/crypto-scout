package com.github.akarazhev.cryptoscout.bybit;

import java.util.List;
import java.util.Objects;

public record Announcement(String title, String description, Type type, List<String> tags, String url,
                           long dateTimestamp, long publishTime) {

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (Announcement) o;
        return dateTimestamp == that.dateTimestamp &&
                publishTime == that.publishTime &&
                Objects.equals(type, that.type) &&
                Objects.equals(url, that.url) &&
                Objects.equals(title, that.title) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, type, tags, url, dateTimestamp, publishTime);
    }

    @Override
    public String toString() {
        return '{' +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", tags=" + tags +
                ", url='" + url + '\'' +
                ", dateTimestamp=" + dateTimestamp +
                ", publishTime=" + publishTime +
                '}';
    }
}
