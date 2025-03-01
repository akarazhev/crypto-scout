package com.github.akarazhev.cryptoscout.bybit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

abstract class BaseAnnouncementFetcher implements AnnouncementFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAnnouncementFetcher.class);
    private static final int PAGE_SIZE = 1000;

    protected final RestTemplate restTemplate;
    protected final String url;
    protected final String locale;
    protected final String category;

    BaseAnnouncementFetcher(final RestTemplate restTemplate, final String url, final String locale, final String category) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.locale = locale;
        this.category = category;
    }

    @Override
    public List<Announcement> fetch(final int page) {
        return getAnnouncements(url + getPath(), getParams(page, PAGE_SIZE));
    }

    @Override
    public int getTotal() {
        return fetch(1).size();
    }

    private List<Announcement> getAnnouncements(final String url, final Map<String, Object> params) {
        try {
            final var entity = restTemplate.getForEntity(url, Response.class, params);
            if (entity.getStatusCode().is2xxSuccessful()) {
                final var response = entity.getBody();
                if (response != null && response.retCode() == 0) {
                    return response.result().list();
                }
            }
            LOGGER.warn("Failed to fetch announcements. Status: {}", entity.getStatusCode());
        } catch (final Exception e) {
            LOGGER.error("Error fetching announcements", e);
        }

        return List.of();
    }
}