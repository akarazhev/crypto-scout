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