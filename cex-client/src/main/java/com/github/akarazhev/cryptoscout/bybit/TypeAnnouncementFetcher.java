package com.github.akarazhev.cryptoscout.bybit;

import org.springframework.web.client.RestTemplate;

import java.util.Map;

final class TypeAnnouncementFetcher extends BaseAnnouncementFetcher {

    TypeAnnouncementFetcher(final RestTemplate restTemplate, final String url, final String locale, final String type) {
        super(restTemplate, url, locale, type);
    }

    @Override
    public String getPath() {
        return "?locale={locale}&type={type}&page={page}&limit={limit}";
    }

    @Override
    public Map<String, Object> getParams(final int page, final int limit) {
        return Map.of("locale", locale, "type", category, "page", page, "limit", limit);
    }
}