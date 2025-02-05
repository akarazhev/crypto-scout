package com.github.akarazhev.cryptoscout.bybit;

import com.github.akarazhev.cryptoscout.Event;
import com.github.akarazhev.cryptoscout.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
class BybitEventSource implements EventSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitEventSource.class);
    private static final int PAGE_SIZE = 1000;
    private final RestTemplate restTemplate;
    private final String url;
    private final String locale;
    private final String[] tags;
    private final String[] types;

    public BybitEventSource(final RestTemplateBuilder builder,
                            @Value("${bybit.announcements.url}") final String url,
                            @Value("${bybit.announcements.locale}") final String locale,
                            @Value("${bybit.announcements.tags}") final String tags,
                            @Value("${bybit.announcements.types}") final String types) {
        this.restTemplate = builder.build();
        this.url = url.trim();
        this.locale = locale.trim();
        this.tags = tags.trim().split(",");
        this.types = types.trim().split(",");
        LOGGER.info("Initializing BybitEventSource with locale: '{}', tags: '{}', types: '{}'", locale, tags, types);
    }

    @Override
    public Stream<Event> getEvents() {
        final var events = new LinkedList<Event>();
        for (final var tag : tags) {
            IntStream.rangeClosed(1, (int) Math.ceil((double) getTotalByTag(tag) / PAGE_SIZE))
                    .boxed()
                    .flatMap(page -> getByTag(tag, page).stream())
                    .map(this::getEvent)
                    .forEach(events::add);
        }

        for (final var type : types) {
            IntStream.rangeClosed(1, (int) Math.ceil((double) getTotalByType(type) / PAGE_SIZE))
                    .boxed()
                    .flatMap(page -> getByType(type, page).stream())
                    .map(this::getEvent)
                    .forEach(events::add);
        }

        return events.stream();
    }

    private int getTotalByTag(final String tag) {
        var entity = restTemplate.getForEntity(url + pathWithTag(), Response.class,
                getTagAndPagination(tag, 1, 1));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().total();
            }
        }

        return 0;
    }

    private int getTotalByType(final String type) {
        var entity = restTemplate.getForEntity(url + pathWithType(), Response.class,
                getTypeAndPagination(type, 1, 1));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().total();
            }
        }

        return 0;
    }

    private List<Announcement> getByTag(final String tag, int page) {
        var entity = restTemplate.getForEntity(url + pathWithTag(), Response.class,
                getTagAndPagination(tag, page, BybitEventSource.PAGE_SIZE));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().list();
            }
        }

        return List.of();
    }

    private List<Announcement> getByType(final String type, int page) {
        var entity = restTemplate.getForEntity(url + pathWithType(), Response.class,
                getTypeAndPagination(type, page, BybitEventSource.PAGE_SIZE));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().list();
            }
        }

        return List.of();
    }

    private String pathWithTag() {
        return "?locale={locale}&tag={tag}&page={page}&limit={limit}";
    }

    private String pathWithType() {
        return "?locale={locale}&type={type}&page={page}&limit={limit}";
    }

    private Map<String, Object> getTagAndPagination(final String tag, final int page, final int limit) {
        return Map.of("locale", locale, "tag", tag, "page", page, "limit", limit);
    }

    private Map<String, Object> getTypeAndPagination(final String type, int page, int limit) {
        return Map.of("locale", locale, "type", type, "page", page, "limit", limit);
    }

    private Event getEvent(final Announcement announcement) {
        return new Event(announcement.dateTimestamp(), announcement.publishTime(), announcement.startDateTimestamp(),
                announcement.endDateTimestamp(), announcement.title(), announcement.description());
    }
}
