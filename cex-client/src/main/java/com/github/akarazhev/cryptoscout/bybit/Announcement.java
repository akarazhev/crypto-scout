package com.github.akarazhev.cryptoscout.bybit;

import java.util.List;

record Announcement(String title, String description, Type type, List<String> tags, String url,
                    long dateTimestamp, long startDateTimestamp, long endDateTimestamp, long publishTime) {
}
