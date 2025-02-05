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
class BybitEventSource implements EventSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitEventSource.class);
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final String url;
    private final String locale;
    private final List<AnnouncementFetcher> fetchers;

    public BybitEventSource(final RestTemplateBuilder builder,
                            @Value("${bybit.announcements.url}") final String url,
                            @Value("${bybit.announcements.locale}") final String locale,
                            @Value("${bybit.announcements.tags}") final String tags,
                            @Value("${bybit.announcements.types}") final String types) {
        this.restTemplate = builder.build();
        this.url = url.trim();
        this.locale = locale.trim();
        this.fetchers = createFetchers(tags.trim().split(","), types.trim().split(","));
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
        return new Event(Event.Platform.BYBIT, announcement.dateTimestamp(), announcement.publishTime(),
                announcement.startDateTimestamp(), announcement.endDateTimestamp(), announcement.title(),
                announcement.description(), announcement.type().key(), announcement.tags(), announcement.url());
    }
}