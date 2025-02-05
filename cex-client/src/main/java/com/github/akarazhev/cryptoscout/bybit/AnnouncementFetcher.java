package com.github.akarazhev.cryptoscout.bybit;

import java.util.List;
import java.util.Map;

interface AnnouncementFetcher {

    List<Announcement> fetch(final int page);

    int getTotal();

    String getPath();

    Map<String, Object> getParams(final int page, final int limit);
}
