/*
 * MIT License
 *
 * Copyright (c) 2025 Andrey Karazhev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.cryptoscout.Event;
import com.github.akarazhev.cryptoscout.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
final class BybitEventSource implements EventSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitEventSource.class);
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final String url;
    private final String locale;
    private final String[] tags;
    private final String[] types;
    private final List<AnnouncementFetcher> fetchers;

    public BybitEventSource(final RestTemplateBuilder builder,
                            @Value("${bybit.announcements.url}") final String url,
                            @Value("${bybit.announcements.locale}") final String locale,
                            @Value("${bybit.announcements.tags}") final String tags,
                            @Value("${bybit.announcements.types}") final String types) {
        this.restTemplate = builder.build();
        this.url = url.trim();
        this.locale = locale.trim();
        this.tags = valueAsArray(tags);
        this.types = valueAsArray(types);
        this.fetchers = createFetchers(this.tags, this.types);
        LOGGER.info("Initializing BybitEventSource with locale: '{}', types: '{}', tags: '{}'", locale, types, tags);
    }

    private List<AnnouncementFetcher> createFetchers(final String[] tags, final String[] types) {
        final var fetchers = new ArrayList<AnnouncementFetcher>();
        for (final var tag : tags) {
            fetchers.add(new TagAnnouncementFetcher(restTemplate, url, locale, tag));
        }

        for (final var type : types) {
            fetchers.add(new TypeAnnouncementFetcher(restTemplate, url, locale, type));
        }

        return fetchers;
    }

    @Override
    public Stream<Event> getEvents() {
        return fetchers.parallelStream().flatMap(this::getEventsFromFetcher);
    }

    private Stream<Event> getEventsFromFetcher(final AnnouncementFetcher fetcher) {
        final var total = fetcher.getTotal();
        return IntStream.rangeClosed(1, (int) Math.ceil((double) total / PAGE_SIZE))
                .boxed()
                .flatMap(page -> fetcher.fetch(page).stream())
                .map(this::getEvent);
    }

    private Event getEvent(final Announcement announcement) {
        return new Event(Event.Platform.BYBIT, announcement.dateTimestamp(), getType(announcement), announcement.title(),
                announcement.description(), announcement.url());
    }

    private String[] valueAsArray(final String value) {
        final var trimmedValue = value.trim();
        return !trimmedValue.isEmpty() ? trimmedValue.split(",") : new String[0];
    }

    private String getType(final Announcement announcement) {
        for (final var tag : tags) {
            if (announcement.tags().contains(tag)) {
                return tag;
            }
        }

        for (final var type : types) {
            if (type.equals(announcement.type().key())) {
                return type;
            }
        }

        return "Unknown";
    }
}