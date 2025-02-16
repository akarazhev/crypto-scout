package com.github.akarazhev.cryptoscout.bybit;

record Type(String title, String key) {

    @Override
    public String toString() {
        return '{' +
                "title='" + title + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
