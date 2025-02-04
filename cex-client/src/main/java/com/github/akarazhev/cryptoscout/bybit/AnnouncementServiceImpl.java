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
    private final RestTemplate restTemplate;
    private final String serviceBybitHost;
    private final String apiBybitAnnouncements;

    public AnnouncementServiceImpl(final RestTemplateBuilder builder,
                                   @Value("${service.bybit.host}") final String serviceBybitHost,
                                   @Value("${api.bybit.announcements}") final String apiBybitAnnouncements) {
        this.restTemplate = builder.build();
        this.serviceBybitHost = serviceBybitHost;
        this.apiBybitAnnouncements = apiBybitAnnouncements;
        LOGGER.info("Bybit announcement service initialized with host: {}", serviceBybitHost);
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
        var entity = restTemplate.getForEntity(serviceBybitHost + apiBybitAnnouncements, Response.class,
                Map.of("locale", "en-US", "page", 1, "limit", 1));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().total();
            }
        }

        return 0;
    }

    private List<Announcement> getAnnouncements(int page, int limit) {
        var entity = restTemplate.getForEntity(serviceBybitHost + apiBybitAnnouncements, Response.class,
                Map.of("locale", "en-US", "page", page, "limit", limit));
        if (entity.getStatusCode().is2xxSuccessful()) {
            var response = entity.getBody();
            if (Objects.requireNonNull(response).retCode() == 0) {
                return response.result().list();
            }
        }

        return List.of();
    }
}
