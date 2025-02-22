package com.github.akarazhev.cryptoscout.bybit;

import java.util.Objects;

record Type(String title, String key) {

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var type = (Type) o;
        return Objects.equals(key, type.key) &&
                Objects.equals(title, type.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, key);
    }

    @Override
    public String toString() {
        return '{' +
                "title='" + title + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
