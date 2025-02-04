package com.github.akarazhev.cryptoscout.bybit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
final class AnnouncementServiceImpl implements AnnouncementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementServiceImpl.class);
    private static final String BY_TYPE_API = "/v5/announcements/index?locale={locale}&type={type}&page={page}&limit={limit}";
    private static final String BY_TAG_API = "/v5/announcements/index?locale={locale}&tag={tag}&page={page}&limit={limit}";
    private final RestTemplate restTemplate;
    private final String host;
    private final String locale;

    public AnnouncementServiceImpl(final RestTemplateBuilder builder,
                                   @Value("${bybit.host}") final String host,
                                   @Value("${bybit.announcements.locale}") final String locale) {
        this.restTemplate = builder.build();
        this.host = host;
        this.locale = locale;
        LOGGER.info("Bybit announcement service initialized with host: {} and locale: {}", host, locale);
    }

    @Override
    public Stream<Announcement> getAnnouncements() {
        final var total = getTotal();
        final var pageSize = 1000; // You can adjust this value as needed
        final var totalPages = (int) Math.ceil((double) total / pageSize);

        return IntStream.rangeClosed(1, totalPages)
                .boxed()
                .flatMap(page -> getAnnouncements(page, pageSize).stream());
    }

    private int getTotal() {
        var entity = restTemplate.getForEntity(host + BY_TAG_API, Response.class,
                getParameters(1, 1));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().total();
            }
        }

        return 0;
    }

    private List<Announcement> getAnnouncements(int page, int limit) {
        var entity = restTemplate.getForEntity(host + BY_TAG_API, Response.class,
                getParameters(page, limit));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().list();
            }
        }

        return List.of();
    }

    private Map<String, Object> getParameters(int page, int limit) {
        return Map.of("locale", locale, "tag", "Launchpool", "page", page, "limit", limit);
    }
}
