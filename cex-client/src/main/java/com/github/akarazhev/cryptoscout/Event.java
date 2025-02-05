package com.github.akarazhev.cryptoscout;

public record Event(long time, long publishTime, long startTime, long endTime, String title, String description) {
}
