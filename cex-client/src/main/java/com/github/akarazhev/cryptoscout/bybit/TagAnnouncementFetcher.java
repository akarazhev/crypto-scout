package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.web.client.RestTemplate;

import java.util.Map;

final class TagAnnouncementFetcher extends BaseAnnouncementFetcher {

    TagAnnouncementFetcher(final RestTemplate restTemplate, final String url, final String locale, final String tag) {
        super(restTemplate, url, locale, tag);
    }

    @Override
    public String getPath() {
        return "?locale={locale}&tag={tag}&page={page}&limit={limit}";
    }

    @Override
    public Map<String, Object> getParams(final int page, final int limit) {
        return Map.of("locale", locale, "tag", category, "page", page, "limit", limit);
    }
}